package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.TransportationRequestDto;
import com.wayble.server.direction.dto.TransportationResponseDto;
import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.entity.Route;
import com.wayble.server.direction.repository.EdgeRepository;
import com.wayble.server.direction.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransportationService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final EdgeService edgeService;

    public TransportationResponseDto findRoutes(TransportationRequestDto request){

        TransportationRequestDto.Location origin = request.origin();
        TransportationRequestDto.Location destination = request.destination();

        Node start = new Node(-1L, origin.name(), origin.latitude(), origin.longitude());
        Node end = new Node(-2L, destination.name(), destination.latitude(), destination.longitude());

        List<TransportationResponseDto.Step> steps = returnDijstra(start, end);


        int startIndex = (request.cursor() != null) ? request.cursor() : 0;
        int pageSize = request.size()    != null ? request.size()    : steps.size();
        int endIndex = Math.min(startIndex + pageSize, steps.size());
        boolean hasNext = endIndex < steps.size();
        Integer nextCursor = hasNext ? endIndex : null;
        TransportationResponseDto.PageInfo pageInfo = new TransportationResponseDto.PageInfo(nextCursor, hasNext);

        return new TransportationResponseDto(steps, pageInfo);
    }

    private List<TransportationResponseDto.Step> returnDijstra(Node startTmp, Node endTmp){

        // 실제 노드·엣지 조회 및 컬렉션 복제
        List<Node> nodes = new ArrayList<>(nodeRepository.findAll());
        List<Edge> edges = new ArrayList<>(edgeRepository.findAll());

        // 임시 노드를 리스트에 추가
        nodes.add(startTmp);
        nodes.add(endTmp);

        // 가장 가까운 실제 정류장 찾기
        Node nearestToStart = nodes.stream()
                .filter(n -> !n.equals(startTmp) && !n.equals(endTmp))
                .min(Comparator.comparingDouble(n ->
                        haversine(startTmp.getLatitude(), startTmp.getLongitude(),
                                n.getLatitude(),   n.getLongitude())))
                .orElseThrow(() -> new NoSuchElementException("시작 기준 가장 가까운 노드 없음"));

        Node nearestToEnd = nodes.stream()
                .filter(n -> !n.equals(startTmp) && !n.equals(endTmp))
                .min(Comparator.comparingDouble(n ->
                        haversine(endTmp.getLatitude(), endTmp.getLongitude(),
                                n.getLatitude(),   n.getLongitude())))
                .orElseThrow(() -> new NoSuchElementException("도착 기준 가장 가까운 노드 없음"));

        // 임시 노드 ↔ 실제 노드 간 엣지 생성
        //    — Edge 클래스에 weight 필드가 없으므로, DirectionType 으로 구분하거나
        //      임시 Map<NodePair, Integer> 에 가중치를 보관합니다.
        //    — 예시에선 DirectionType.UNSPECIFIED 로 두고, 가중치는 로컬 Map 에 저장

        // 로컬에 가중치 보관용 Map

        Map<Pair<Node,Node>, Integer> weightMap = new HashMap<>();

        int weightStart = (int)(haversine(
                startTmp.getLatitude(), startTmp.getLongitude(),
                nearestToStart.getLatitude(), nearestToStart.getLongitude()
        ) * 1000);
        weightMap.put(Pair.of(startTmp, nearestToStart), weightStart);
        weightMap.put(Pair.of(nearestToStart, startTmp), weightStart);

        int weightEnd = (int)(haversine(
                endTmp.getLatitude(), endTmp.getLongitude(),
                nearestToEnd.getLatitude(), nearestToEnd.getLongitude()
        ) * 1000);
        weightMap.put(Pair.of(nearestToEnd, endTmp), weightEnd);
        weightMap.put(Pair.of(endTmp, nearestToEnd), weightEnd);

        // Edge 객체 생성: route는 null, edgeType 은 필요에 따라 설정
        edges.add(edgeService.createEdge(-1L, startTmp, nearestToStart, DirectionType.FROM_WAYPOINT));
        edges.add(edgeService.createEdge(-2L, nearestToStart, startTmp, DirectionType.TO_WAYPOINT));
        edges.add(edgeService.createEdge(-3L, nearestToEnd, endTmp, DirectionType.FROM_WAYPOINT));
        edges.add(edgeService.createEdge(-4L, endTmp, nearestToEnd, DirectionType.TO_WAYPOINT));

        // 그래프 빌드 및 Dijkstra 호출
        Map<Long, List<Edge>> graph = buildGraph(nodes, edges);
        List<TransportationResponseDto.Step> steps = runDijstra(graph, startTmp, endTmp);

        return steps;
    }

    private List<TransportationResponseDto.Step> runDijstra(Map<Long, List<Edge>> graph, Node start, Node end){

        List<Node> nodes = nodeRepository.findAll();
        List<Edge> edges = edgeRepository.findAll();

        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Edge> prevEdge = new HashMap<>();
        Map<Long, Node> prevNode = new HashMap<>();
        for (Node node : nodes) distance.put(node.getId(), Integer.MAX_VALUE);
        distance.put(start.getId(), 0);

        // 그래프 구성: 각 노드에서 출발 가능한 엣지 목록
        for (Edge edge : edges) {
            Long startId = edge.getStartNode() != null ? edge.getStartNode().getId() : null;
            if (startId == null) {
                System.out.println("❗ edge의 startNode 또는 startNode.id가 null입니다. edgeId=" + edge.getId());
                continue;
            }
            if (!graph.containsKey(startId)) {
                System.out.println("❗ graph에 해당 startId가 없습니다: " + startId);
                continue;
            }
            graph.get(startId).add(edge);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(distance::get));
        pq.add(start);

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            if (curr.equals(end)) break;
            for (Edge edge : graph.getOrDefault(curr, List.of())) {
                Node neighbor = edge.getEndNode();
                // 가중치 계산: 노드간 거리 기반
                double weight = haversine(
                        edge.getStartNode().getLatitude(), edge.getStartNode().getLongitude(),
                        edge.getEndNode().getLatitude(),   edge.getEndNode().getLongitude()
                );
                int alt = distance.get(curr) + (int)(weight * 1000);  // 미터 단위 정수화
                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor.getId(), alt);
                    prevNode.put(neighbor.getId(), curr);
                    prevEdge.put(neighbor.getId(), edge);
                    pq.add(neighbor);
                }
            }
        }

        // 역추적해 경로 steps 생성
        List<TransportationResponseDto.Step> steps = new LinkedList<>();
        Node current = end;
        while (!current.equals(start)) {
            Edge edge = prevEdge.get(current);
            if (edge == null) break; // 경로 없음
            String routeName = (edge.getRoute() != null) ? edge.getRoute().getRouteName() : null;
            steps.add(0, new TransportationResponseDto.Step(
                    edge.getEdgeType(),
                    routeName,
                    edge.getStartNode().getStationName(),
                    edge.getEndNode().getStationName()
            ));
            current = prevNode.get(current);
        }

        return steps;

    }

    private Map<Long, List<Edge>> buildGraph(List<Node> nodes, List<Edge> edges) {
        Map<Long, List<Edge>> graph = new HashMap<>();
        for (Node node : nodes) {
            Long nodeId = node.getId();
            if (nodeId != null) {
                graph.put(nodeId, new ArrayList<>());
            } else {
                System.out.println("❗ ID가 null인 node 발견: " + node.getStationName());
            }
        }
        for (Edge edge : edges) {
            Node start = edge.getStartNode();
            Long startId = start != null ? start.getId() : null;
            if (startId == null) {
                System.out.println("❗ edge의 startNode 또는 startId가 null. edge=" + edge);
                continue;
            }

            if (!graph.containsKey(startId)) {
                System.out.println("❗ graph에 없는 startId: " + startId);
                continue;
            }
            graph.get(edge.getStartNode().getId()).add(edge);
        }
        return graph;
    }

    public static double haversine(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        final int R = 6_371; // 지구 반지름 (km)
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double Δφ = Math.toRadians(lat2 - lat1);
        double Δλ = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
                + Math.cos(φ1) * Math.cos(φ2)
                * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // km 단위 거리 반환
    }

}
