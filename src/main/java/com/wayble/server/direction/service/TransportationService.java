package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.dto.request.TransportationRequestDto;
import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.transportation.Edge;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.repository.EdgeRepository;
import com.wayble.server.direction.repository.NodeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.wayble.server.direction.exception.DirectionErrorCase.PATH_NOT_FOUND;
import static com.wayble.server.direction.exception.DirectionErrorCase.DISTANCE_TOO_FAR;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportationService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final FacilityService facilityService;
    private final BusInfoService busInfoService;

    private List<Node> nodes;
    private List<Edge> edges;

    private static final int TRANSFER_PENALTY = 2000;
    private static final int STEP_PENALTY = 500;
    private static final int METER_CONVERSION = 1000;
    private static final double DISTANCE_CONSTRAINT = 30;

    public TransportationResponseDto findRoutes(TransportationRequestDto request){

        TransportationRequestDto.Location origin = request.origin();
        TransportationRequestDto.Location destination = request.destination();

        // 거리 검증 (30km 제한)
        double distance = haversine(origin.latitude(), origin.longitude(), 
                                  destination.latitude(), destination.longitude());
        if (distance >= DISTANCE_CONSTRAINT) {
            throw new ApplicationException(DISTANCE_TOO_FAR);
        }

        Node start = new Node(-1L, origin.name(), DirectionType.FROM_WAYPOINT ,origin.latitude(), origin.longitude());
        Node end = new Node(-2L, destination.name(), DirectionType.TO_WAYPOINT,destination.latitude(), destination.longitude());

        List<TransportationResponseDto.Step> steps = returnDijkstra(start, end);


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


    private List<TransportationResponseDto.Step> returnDijkstra(Node startTmp, Node endTmp){

        // 실제 노드·엣지 조회 및 컬렉션 복제
        nodes = new ArrayList<>(nodeRepository.findAll());
        edges = new ArrayList<>(edgeRepository.findAll());

        // 가장 가까운 실제 정류장 찾기 (임시 노드 추가 전에)
        Node nearestToStart = nodes.stream()
                .min(Comparator.comparingDouble(n ->
                        haversine(startTmp.getLatitude(), startTmp.getLongitude(),
                                n.getLatitude(), n.getLongitude())))
                .orElseThrow(() -> new ApplicationException(PATH_NOT_FOUND));

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
        Edge startToStation = Edge.createEdge(-1L, startTmp, nearestToStart, DirectionType.WALK);
        edges.add(startToStation);

        // 가장 가까운 정류장 -> 도착지 (도보)
        Edge stationToEnd = Edge.createEdge(-2L, nearestToEnd, endTmp, DirectionType.WALK);
        edges.add(stationToEnd);

        // 모든 엣지의 가중치 계산
        for (Edge edge : edges) {
            if (edge == null) continue;

            Node from = edge.getStartNode();
            Node to = edge.getEndNode();

            if (from == null || to == null || from.getId() == null || to.getId() == null) {
                continue;
            }

            int weight = (int)(haversine(
                    from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude()
            ) * METER_CONVERSION);

            weightMap.put(Pair.of(from.getId(), to.getId()), weight);
        }

        // 그래프 빌드 및 Dijkstra 호출
        Map<Long, List<Edge>> graph = buildGraph(nodes, edges);
        List<TransportationResponseDto.Step> result = runDijkstra(graph, startTmp, endTmp, weightMap);

        return result;
    }

    private List<TransportationResponseDto.Step> runDijkstra(
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

        while (!pq.isEmpty()) {
            Node curr = pq.poll();

            if (visited.contains(curr.getId())) {
                continue;
            }
            visited.add(curr.getId());

            if (curr.equals(end)) {
                break;
            }

            for (Edge edge : graph.getOrDefault(curr.getId(), List.of())) {
                if (edge == null || edge.getEndNode() == null) continue;

                Node neighbor = edge.getEndNode();
                if (visited.contains(neighbor.getId())) continue;

                if (edge.getStartNode() == null || edge.getEndNode() == null ||
                        edge.getStartNode().getId() == null || edge.getEndNode().getId() == null) {
                    continue;
                }

                Pair<Long, Long> key = Pair.of(edge.getStartNode().getId(), edge.getEndNode().getId());
                int baseWeight = weightMap.getOrDefault(key,
                        (int)(haversine(
                                edge.getStartNode().getLatitude(), edge.getStartNode().getLongitude(),
                                edge.getEndNode().getLatitude(), edge.getEndNode().getLongitude()
                        ) * METER_CONVERSION)
                );

                // 간단한 경로 선호를 위한 가중치 조정
                int weight = baseWeight;

                // 환승 패널티 (교통수단 변경 시 추가 비용)
                Edge prevEdgeForCurr = prevEdge.get(curr.getId());
                if (prevEdgeForCurr != null &&
                        prevEdgeForCurr.getEdgeType() != edge.getEdgeType() &&
                        prevEdgeForCurr.getEdgeType() != DirectionType.WALK &&
                        edge.getEdgeType() != DirectionType.WALK) {
                    weight += TRANSFER_PENALTY; // 환승 패널티 대폭 증가
                }

                // 단계 수 패널티 (경로 단계가 많을수록 불이익)
                weight += STEP_PENALTY;

                int alt = distance.get(curr.getId()) + weight;
                if (alt < distance.get(neighbor.getId())) {
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
        Set<Long> backtrackVisited = new HashSet<>();

        if (distance.get(end.getId()) == Integer.MAX_VALUE) {
            log.info("경로를 찾을 수 없음: 도착지에 도달할 수 없음");
            return steps; // 빈 리스트 반환
        }

        // 먼저 모든 엣지를 수집
        List<Edge> pathEdges = new ArrayList<>();
        while (current != null && !current.equals(start)) {
            if (backtrackVisited.contains(current.getId())) {
                break;
            }
            backtrackVisited.add(current.getId());

            Edge edge = prevEdge.get(current.getId());
            if (edge == null) {
                break;
            }
            pathEdges.add(0, edge);
            current = prevNode.get(current.getId());
        }

        // 연속된 같은 노선의 구간들을 합치기
        return mergeConsecutiveRoutes(pathEdges);

    }

    private List<TransportationResponseDto.Step> mergeConsecutiveRoutes(List<Edge> pathEdges) {
        List<TransportationResponseDto.Step> mergedSteps = new ArrayList<>();
        
        if (pathEdges.isEmpty()) {
            return mergedSteps;
        }
        
        int i = 0;
        while (i < pathEdges.size()) {
            Edge currentEdge = pathEdges.get(i);
            DirectionType currentType = currentEdge.getEdgeType();
            
            // 시작 노드 이름
            String fromName = (currentEdge.getStartNode() != null && currentEdge.getStartNode().getStationName() != null) 
                ? currentEdge.getStartNode().getStationName() : "Unknown";
            String toName = (currentEdge.getEndNode() != null && currentEdge.getEndNode().getStationName() != null) 
                ? currentEdge.getEndNode().getStationName() : "Unknown";
            
            // 도보 처리
            if (currentType == DirectionType.WALK) {
                mergedSteps.add(new TransportationResponseDto.Step(
                    currentType,
                    null,
                    null,
                    0,
                    null,
                    null,
                    fromName,
                    toName
                ));
                i++;
                continue;
            }
            
            // 동일 타입 + 동일 Route 객체 그룹화
            int j = i + 1;
            while (j < pathEdges.size()) {
                Edge nextEdge = pathEdges.get(j);
                if (nextEdge.getEdgeType() != currentType) break;
                if (!Objects.equals(currentEdge.getRoute(), nextEdge.getRoute())) break;
                j++;
            }
            
            // 그룹 마지막 엣지 기준 toName
            Edge lastEdgeInGroup = pathEdges.get(j - 1);
            if (lastEdgeInGroup.getEndNode() != null && lastEdgeInGroup.getEndNode().getStationName() != null) {
                toName = lastEdgeInGroup.getEndNode().getStationName();
            }
            
            // moveInfo: 중간 정류장만
            List<TransportationResponseDto.MoveInfo> moveInfoList = new ArrayList<>();
            for (int k = i + 1; k < j; k++) {
                Edge e = pathEdges.get(k);
                if (e.getStartNode() != null && e.getStartNode().getStationName() != null) {
                    moveInfoList.add(new TransportationResponseDto.MoveInfo(e.getStartNode().getStationName()));
                }
            }
            if (moveInfoList.isEmpty()) moveInfoList = null;
            
            // routeName
            String routeName = null;
            for (int k = i; k < j; k++) {
                Edge e = pathEdges.get(k);
                if (e.getRoute() != null && e.getRoute().getRouteName() != null) {
                    routeName = e.getRoute().getRouteName();
                    break;
                }
            }
            
            // busInfo / subwayInfo 설정
            TransportationResponseDto.BusInfo busInfo = null;
            TransportationResponseDto.SubwayInfo subwayInfo = null;
            if (currentType == DirectionType.BUS) {
                boolean isShuttle = routeName != null && routeName.contains("마포"); // 마을버스 구분

                Long stationId = currentEdge.getStartNode() != null ? currentEdge.getStartNode().getId() : null;
                List<Boolean> lowFloors = null;
                List<Integer> intervals = null;
                try {
                    if (stationId != null) {
                        TransportationResponseDto.BusInfo busInfoData = busInfoService.getBusInfo(currentEdge.getStartNode().getStationName(), null, currentEdge.getStartNode().getLatitude(), currentEdge.getStartNode().getLongitude());
                        busInfo = busInfoData;
                    }
                } catch (Exception e) {
                    log.error("버스 정보 조회 실패: {}", e.getMessage(), e);
                }
            } 
            else if (currentType == DirectionType.SUBWAY) {
                Long stationId = currentEdge.getStartNode() != null ? currentEdge.getStartNode().getId() : null;
                try {
                    if (stationId != null) {
                        TransportationResponseDto.NodeInfo nodeInfo = facilityService.getNodeInfo(stationId);
                        subwayInfo = new TransportationResponseDto.SubwayInfo(
                            nodeInfo.wheelchair(),
                            nodeInfo.elevator(),
                            nodeInfo.accessibleRestroom()
                        );
                    }
                } catch (Exception e) {
                    log.warn("지하철역 시설 정보 조회 실패. 역 ID {}: {}", stationId, e.getMessage());
                    subwayInfo = new TransportationResponseDto.SubwayInfo(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        false
                    );
                }
            }
            
            int moveNumber = j - i - 1;
            
            mergedSteps.add(new TransportationResponseDto.Step(
                currentType,
                moveInfoList,
                routeName,
                moveNumber,
                busInfo,
                subwayInfo,
                fromName,
                toName
            ));
            
            i = j;
        }
        
        return mergedSteps;
    }

    private Map<Long, List<Edge>> buildGraph(List<Node> nodes, List<Edge> edges) {
        Map<Long, List<Edge>> graph = new HashMap<>();
        for (Node node : nodes) {
            Long nodeId = node.getId();
            if (nodeId != null) {
                graph.put(nodeId, new ArrayList<>());
            } else {
                log.warn("ID가 null인 node 발견: " + node.getStationName());
            }
        }
        for (Edge edge : edges) {
            if (edge == null) continue;

            Node start = edge.getStartNode();
            if (start == null || start.getId() == null) continue;

            Long startId = start.getId();
            if (!graph.containsKey(startId)) continue;

            graph.get(startId).add(edge);
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
