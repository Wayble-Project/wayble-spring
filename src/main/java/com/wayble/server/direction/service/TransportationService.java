package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.dto.TransportationRequestDto;
import com.wayble.server.direction.dto.TransportationResponseDto;
import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.repository.EdgeRepository;
import com.wayble.server.direction.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.wayble.server.direction.exception.DirectionErrorCase.PATH_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TransportationService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final EdgeService edgeService;

    private List<Node> nodes;
    private List<Edge> edges;

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

        // 경로를 찾지 못한 경우 처리
        if (steps.isEmpty()) {
            throw new ApplicationException(PATH_NOT_FOUND);
        }

        return new TransportationResponseDto(steps, pageInfo);
    }

    private List<TransportationResponseDto.Step> returnDijstra(Node startTmp, Node endTmp){

        // 실제 노드·엣지 조회 및 컬렉션 복제
        nodes = new ArrayList<>(nodeRepository.findAll());
        edges = new ArrayList<>(edgeRepository.findAll());

        // 가장 가까운 실제 정류장 찾기 (임시 노드 추가 전에)
        Node nearestToStart = nodes.stream()
                .min(Comparator.comparingDouble(n ->
                        haversine(startTmp.getLatitude(), startTmp.getLongitude(),
                                n.getLatitude(), n.getLongitude())))
                .orElseThrow(() -> new NoSuchElementException("시작 기준 가장 가까운 노드 없음"));

        // 도착지는 출발지와 다른 정류장을 선택
        Node nearestToEnd = nodes.stream()
                .filter(n -> !n.equals(nearestToStart))
                .min(Comparator.comparingDouble(n ->
                        haversine(endTmp.getLatitude(), endTmp.getLongitude(),
                                n.getLatitude(), n.getLongitude())))
                .orElse(nearestToStart); // fallback to same station if no other option

        // 임시 노드를 리스트에 추가
        nodes.add(startTmp);
        nodes.add(endTmp);

        // 로컬에 가중치 보관용 Map
        Map<Pair<Long,Long>, Integer> weightMap = new HashMap<>();

        // 출발지 -> 가장 가까운 정류장 (도보)
        Edge startToStation = edgeService.createEdge(-1L, startTmp, nearestToStart, DirectionType.WALK);
        edges.add(startToStation);

        // 가장 가까운 정류장 -> 도착지 (도보)
        Edge stationToEnd = edgeService.createEdge(-2L, nearestToEnd, endTmp, DirectionType.WALK);
        edges.add(stationToEnd);

        // 모든 엣지의 가중치 계산
        for (Edge edge : edges) {
            Node from = edge.getStartNode();
            Node to = edge.getEndNode();
            int weight = (int)(haversine(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            ) * 1000);

            weightMap.put(Pair.of(from.getId(), to.getId()), weight);
        }

        // 그래프 빌드 및 Dijkstra 호출
        Map<Long, List<Edge>> graph = buildGraph(nodes, edges);
        List<TransportationResponseDto.Step> result = runDijstra(graph, startTmp, endTmp, weightMap);

        return result;
    }

    private List<TransportationResponseDto.Step> runDijstra(
            Map<Long, List<Edge>> graph, Node start, Node end,
            Map<Pair<Long, Long>, Integer> weightMap
    ){

        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Edge> prevEdge = new HashMap<>();
        Map<Long, Node> prevNode = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        // 초기화
        for (Node node : nodes) {
            distance.put(node.getId(), Integer.MAX_VALUE);
            prevNode.put(node.getId(), null);
            prevEdge.put(node.getId(), null);
        }
        distance.put(start.getId(), 0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> distance.getOrDefault(n.getId(), Integer.MAX_VALUE)));
        pq.add(start);

        // 다익스트라 알고리즘 실행
        while (!pq.isEmpty()) {
            Node curr = pq.poll();

            if (visited.contains(curr.getId())) continue;
            visited.add(curr.getId());

            if (curr.equals(end)) break;

            for (Edge edge : graph.getOrDefault(curr.getId(), List.of())) {
                if (edge == null || edge.getEndNode() == null) continue;

                Node neighbor = edge.getEndNode();
                if (visited.contains(neighbor.getId())) continue;

                Pair<Long, Long> key = Pair.of(edge.getStartNode().getId(), edge.getEndNode().getId());
                int weight = weightMap.getOrDefault(key,
                        (int)(haversine(
                                edge.getStartNode().getLatitude(), edge.getStartNode().getLongitude(),
                                edge.getEndNode().getLatitude(), edge.getEndNode().getLongitude()
                        ) * 1000)
                );

                int alt = distance.get(curr.getId()) + weight;
                if (alt < distance.get(neighbor.getId())) {
                    distance.put(neighbor.getId(), alt);
                    prevNode.put(neighbor.getId(), curr);
                    prevEdge.put(neighbor.getId(), edge);
                    pq.add(neighbor);
                }
            }
        }

        // 역추적으로 경로 생성
        List<TransportationResponseDto.Step> steps = new LinkedList<>();
        Node current = end;
        Set<Long> backtrackVisited = new HashSet<>();

        while (current != null && !current.equals(start)) {
            if (backtrackVisited.contains(current.getId())) {
                break;
            }
            backtrackVisited.add(current.getId());

            Edge edge = prevEdge.get(current.getId());
            if (edge == null) {
                break;
            }

            steps.add(0, new TransportationResponseDto.Step(
                    edge.getEdgeType(),
                    (edge.getRoute() != null) ? edge.getRoute().getRouteName() : null,
                    edge.getStartNode().getStationName(),
                    edge.getEndNode().getStationName()
            ));

            current = prevNode.get(current.getId());
        }
        return steps;

    }

    private Map<Long, List<Edge>> buildGraph(List<Node> nodes, List<Edge> edges) {
        Map<Long, List<Edge>> graph = new HashMap<>();
        for (Node node : nodes) {
            Long nodeId = node.getId();
            if (nodeId != null) {
                graph.put(nodeId, new ArrayList<>());
            }
        }
        for (Edge edge : edges) {
            Node start = edge.getStartNode();
            Long startId = start != null ? start.getId() : null;
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
