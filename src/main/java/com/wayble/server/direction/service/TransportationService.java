package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.dto.TransportationGraphDto;
import com.wayble.server.direction.dto.request.TransportationRequestDto;
import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.transportation.Edge;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.entity.transportation.Route;
import com.wayble.server.direction.repository.EdgeRepository;
import com.wayble.server.direction.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.wayble.server.direction.exception.DirectionErrorCase.PATH_NOT_FOUND;
import static com.wayble.server.direction.exception.DirectionErrorCase.DISTANCE_TOO_FAR;
@Slf4j
@Service
@RequiredArgsConstructor
public class TransportationService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final FacilityService facilityService;
    private final BusInfoService busInfoService;

    private static final int TRANSFER_PENALTY = 10000; // 환승 시 추가되는 가중치 (m)
    private static final int STEP_PENALTY = 500; // 각 이동 단계마다 추가되는 기본 가중치 (m)
    private static final int METER_CONVERSION = 1000; // 킬로미터를 미터로 변환하는 상수
    private static final double DISTANCE_CONSTRAINT = 30; // 최대 이동 가능 거리 제한 (km)
    
    private static final int NEARBY_STATION_WALK_DISTANCE = 2000; // 인근 정류장 간 도보 연결 가능 거리 (미터)
    private static final int ORIGIN_DESTINATION_WALK_DISTANCE = 1000; // 출발지/도착지에서 정류장까지 도보 연결 가능 거리 (m)
    private static final int MAX_NEARBY_NODES = 5; // 출발지/도착지 주변에서 고려할 최대 정류장 수
    private static final int MAX_DIJKSTRA_VISITS = 5000; // 다익스트라 알고리즘에서 방문할 수 있는 최대 노드 수 (무한 루프 방지)
    private static final int MAX_ROUTES = 5; // 찾을 최대 경로 수

    public TransportationResponseDto findRoutes(TransportationRequestDto request){
    
        TransportationRequestDto.Location origin = request.origin();
        TransportationRequestDto.Location destination = request.destination();
        // 1. 거리 검증 (30km 제한)
        double distance = haversine(origin.latitude(), origin.longitude(), 
                                  destination.latitude(), destination.longitude());
        if (distance >= DISTANCE_CONSTRAINT) {
            throw new ApplicationException(DISTANCE_TOO_FAR);
        }

        // 2. 임시 노드 생성
        Node start = Node.createNode(-1L, origin.name(), DirectionType.FROM_WAYPOINT ,origin.latitude(), origin.longitude());
        Node end = Node.createNode(-2L, destination.name(), DirectionType.TO_WAYPOINT,destination.latitude(), destination.longitude());

        // 3. 여러 경로 찾기
        List<List<TransportationResponseDto.Step>> allRoutes = findMultipleTransportationRoutes(start, end);

        // 4. 페이징 처리
        int startIndex = (request.cursor() != null) ? request.cursor() : 0;
        int pageSize = (request.size() != null) ? request.size() : 5; // 기본값 5로 설정
        int endIndex = Math.min(startIndex + pageSize, allRoutes.size());
        boolean hasNext = endIndex < allRoutes.size();
        Integer nextCursor = hasNext ? endIndex : null;
        TransportationResponseDto.PageInfo pageInfo = new TransportationResponseDto.PageInfo(nextCursor, hasNext);

        // 경로를 찾지 못한 경우 처리
        if (allRoutes.isEmpty()) {
            throw new ApplicationException(PATH_NOT_FOUND);
        }

        // 페이징된 경로들을 Route 객체로 변환
        List<TransportationResponseDto.Route> routeList = new ArrayList<>();
        List<List<TransportationResponseDto.Step>> pagedRoutes = allRoutes.subList(startIndex, endIndex);
        for (int i = 0; i < pagedRoutes.size(); i++) {
            List<TransportationResponseDto.Step> route = pagedRoutes.get(i);
            TransportationResponseDto.Route routeObj = createRoute(route, startIndex + i + 1);
            routeList.add(routeObj);
        }

        return new TransportationResponseDto(routeList, pageInfo);
    }

    private TransportationResponseDto.Route createRoute(List<TransportationResponseDto.Step> steps, int routeIndex) {
        return new TransportationResponseDto.Route(routeIndex, steps);
    }

    private List<List<TransportationResponseDto.Step>> findMultipleTransportationRoutes(Node startTmp, Node endTmp){
        // 1. 데이터 로드
        List<Node> nodes = new ArrayList<>(nodeRepository.findAll());
        List<Edge> edges = new ArrayList<>(edgeRepository.findAllWithNodesAndRoute());
        
        // 2. 가장 가까운 정류장 찾기
        Node nearestToStart = findNearestNode(nodes, startTmp.getLatitude(), startTmp.getLongitude());
        Node nearestToEnd = findNearestNode(nodes, endTmp.getLatitude(), endTmp.getLongitude());
        
        if (nearestToStart == null || nearestToEnd == null) {
            throw new ApplicationException(PATH_NOT_FOUND);
        }
        
        // 3. 임시 노드 추가
        nodes.add(startTmp);
        nodes.add(endTmp);

        // 4. 그래프 빌드 및 여러 경로 찾기
        TransportationGraphDto graphData = buildGraph(nodes, edges, startTmp, endTmp);
        return findMultipleOptimalRoutes(graphData.graph(), startTmp, endTmp, graphData.weightMap(), nodes, nearestToStart, nearestToEnd);
    }

    private List<List<TransportationResponseDto.Step>> findMultipleOptimalRoutes(
            Map<Long, List<Edge>> graph, 
            Node startTmp, 
            Node endTmp, 
            Map<Pair<Long, Long>, Integer> weightMap, 
            List<Node> nodes,
            Node nearestToStart,
            Node nearestToEnd) {
        
        // 1. 임시 노드 찾기
        Node startNode = nodes.stream()
                .filter(node -> node.getId().equals(-1L))
                .findFirst()
                .orElse(null);
        
        Node endNode = nodes.stream()
                .filter(node -> node.getId().equals(-2L))
                .findFirst()
                .orElse(null);
        
        if (startNode == null || endNode == null) {
            return new ArrayList<>();
        }
        
        // 2. 여러 경로 찾기
        List<List<TransportationResponseDto.Step>> allRoutes = findMultipleRoutes(graph, startNode, endNode, weightMap, nodes);
        
        // 3. 경로 필터링 및 정렬
        return filterAndSortRoutes(allRoutes);
    }

    private List<List<TransportationResponseDto.Step>> findMultipleRoutes(
            Map<Long, List<Edge>> graph, 
            Node start, 
            Node end, 
            Map<Pair<Long, Long>, Integer> weightMap, 
            List<Node> nodes) {
        
        List<List<TransportationResponseDto.Step>> routes = new ArrayList<>();
        
        // 1. 기본 다익스트라로 첫 번째 경로 찾기
        List<TransportationResponseDto.Step> firstRoute = runDijkstra(graph, start, end, weightMap, nodes);
        if (!firstRoute.isEmpty()) {
            routes.add(firstRoute);
        }
        
        // 2. 효율적인 다중 경로 찾기 - 한 번의 탐색으로 여러 경로 생성
        if (!firstRoute.isEmpty()) {
            List<List<TransportationResponseDto.Step>> alternativeRoutes = findAlternativeRoutesEfficiently(
                graph, start, end, weightMap, nodes, firstRoute
            );
            routes.addAll(alternativeRoutes);
        }
        
        return routes;
    }

    private List<List<TransportationResponseDto.Step>> findAlternativeRoutesEfficiently(
            Map<Long, List<Edge>> graph, 
            Node start, 
            Node end, 
            Map<Pair<Long, Long>, Integer> weightMap, 
            List<Node> nodes,
            List<TransportationResponseDto.Step> firstRoute) {
        
        List<List<TransportationResponseDto.Step>> alternativeRoutes = new ArrayList<>();
        
        // 첫 번째 경로에서 실제 사용된 엣지들을 추출
        Set<Pair<Long, Long>> usedEdges = extractActualEdgesFromRoute(firstRoute, graph);
        
        // 최대 4개의 추가 경로 찾기
        for (int i = 0; i < 4 && alternativeRoutes.size() < MAX_ROUTES - 1; i++) {
            // 실제 사용된 엣지들에만 패널티를 적용한 가중치 맵 생성
            Map<Pair<Long, Long>, Integer> penalizedWeightMap = createActualEdgePenalizedWeightMap(weightMap, usedEdges, i + 1);
            
            // 다익스트라로 새로운 경로 찾기
            List<TransportationResponseDto.Step> newRoute = runDijkstra(graph, start, end, penalizedWeightMap, nodes);
            
            if (newRoute.isEmpty()) {
                break;
            }
            
            // 첫 번째 경로와 동일한지 확인
            if (areRoutesIdentical(newRoute, firstRoute)) {
                continue;
            }
            
            // 새로운 경로에서 사용된 엣지들도 추가
            Set<Pair<Long, Long>> newUsedEdges = extractActualEdgesFromRoute(newRoute, graph);
            usedEdges.addAll(newUsedEdges);
            
            alternativeRoutes.add(newRoute);
        }
        
        return alternativeRoutes;
    }





    private Set<Pair<Long, Long>> extractActualEdgesFromRoute(List<TransportationResponseDto.Step> route, Map<Long, List<Edge>> graph) {
        Set<Pair<Long, Long>> usedEdges = new HashSet<>();
        
        for (TransportationResponseDto.Step step : route) {
            String fromName = step.from();
            String toName = step.to();
            
            for (Map.Entry<Long, List<Edge>> entry : graph.entrySet()) {
                Long nodeId = entry.getKey();
                List<Edge> edges = entry.getValue();
                
                for (Edge edge : edges) {
                    Node fromNode = edge.getStartNode();
                    Node toNode = edge.getEndNode();
                    
                    if ((fromNode.getStationName().equals(fromName) && toNode.getStationName().equals(toName)) ||
                        (fromNode.getStationName().equals(toName) && toNode.getStationName().equals(fromName))) {
                        usedEdges.add(Pair.of(fromNode.getId(), toNode.getId()));
                        usedEdges.add(Pair.of(toNode.getId(), fromNode.getId()));
                    }
                }
            }
        }
        
        return usedEdges;
    }
    
    private Map<Pair<Long, Long>, Integer> createActualEdgePenalizedWeightMap(Map<Pair<Long, Long>, Integer> originalWeightMap, Set<Pair<Long, Long>> usedEdges, int routeIndex) {
        Map<Pair<Long, Long>, Integer> penalizedWeightMap = new HashMap<>();
        
        for (Map.Entry<Pair<Long, Long>, Integer> entry : originalWeightMap.entrySet()) {
            Pair<Long, Long> edge = entry.getKey();
            int weight = entry.getValue();
            
            if (usedEdges.contains(edge)) {
                int penalty = routeIndex * 100000;
                penalizedWeightMap.put(edge, weight + penalty);
            } else {
                penalizedWeightMap.put(edge, weight);
            }
        }
        
        return penalizedWeightMap;
    }
    
    private boolean areRoutesIdentical(List<TransportationResponseDto.Step> route1, List<TransportationResponseDto.Step> route2) {
        // 두 경로가 완전히 동일한지 확인
        if (route1.size() != route2.size()) {
            return false;
        }
        
        for (int i = 0; i < route1.size(); i++) {
            TransportationResponseDto.Step step1 = route1.get(i);
            TransportationResponseDto.Step step2 = route2.get(i);
            
            if (step1.mode() != step2.mode() || 
                !Objects.equals(step1.from(), step2.from()) || 
                !Objects.equals(step1.to(), step2.to()) ||
                !Objects.equals(step1.routeName(), step2.routeName())) {
                return false;
            }
        }
        
        return true;
    }

    private List<List<TransportationResponseDto.Step>> filterAndSortRoutes(List<List<TransportationResponseDto.Step>> routes) {
        return routes.stream()
                .filter(route -> {
                    // 대중교통 포함 여부 확인
                    boolean hasPublicTransport = route.stream()
                            .anyMatch(step -> step.mode() == DirectionType.BUS || step.mode() == DirectionType.SUBWAY);
                    
                    if (!hasPublicTransport) {
                        return false;
                    }
                    
                    // 환승 횟수 검증 (3회 이상 제외)
                    int transferCount = calculateTransferCount(route);
                    return transferCount < 3;
                })
                .sorted(Comparator
                        .<List<TransportationResponseDto.Step>>comparingInt(this::calculateTransferCount)
                        .thenComparingInt(this::calculateWalkDistance))
                .limit(MAX_ROUTES)
                .collect(Collectors.toList());
    }

    private int calculateWalkDistance(List<TransportationResponseDto.Step> route) {
        return route.stream()
                .filter(step -> step.mode() == DirectionType.WALK)
                .mapToInt(step -> {
                    // 간단한 도보 거리 추정 (실제로는 정확한 거리 계산 필요)
                    return 500; // 기본값
                })
                .sum();
    }

    private TransportationGraphDto buildGraph(List<Node> nodes, List<Edge> edges, Node startTmp, Node endTmp) {
        Map<Long, List<Edge>> graph = new HashMap<>();
        Map<Pair<Long, Long>, Integer> weightMap = new HashMap<>();
        
        // 1. 노드 초기화
        for (Node node : nodes) {
            Long nodeId = node.getId();
            if (nodeId != null) {
                graph.put(nodeId, new ArrayList<>());
            }
        }
        
        // 2. 기존 엣지 추가 및 가중치 계산
        for (Edge edge : edges) {
            if (edge == null) continue;

            Node start = edge.getStartNode();
            Node end = edge.getEndNode();
            if (start == null || end == null || start.getId() == null || end.getId() == null) continue;

            Long startId = start.getId();
            Long endId = end.getId();
            
            if (!graph.containsKey(startId)) continue;

            graph.get(startId).add(edge);
            
            int weight = (int)(haversine(
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude()
            ) * METER_CONVERSION);
            weightMap.put(Pair.of(startId, endId), weight);
        }
        
        // 3. 출발지/도착지 도보 연결 추가
        addOriginDestinationWalkConnections(graph, weightMap, nodes, startTmp, endTmp);
        
        return new TransportationGraphDto(graph, weightMap);
    }

    private void addOriginDestinationWalkConnections(Map<Long, List<Edge>> graph, Map<Pair<Long, Long>, Integer> weightMap, List<Node> nodes, Node startTmp, Node endTmp) {
        // 1. 임시 노드 생성
        Node startNode = Node.createNode(-1L, startTmp.getStationName(), DirectionType.WALK, 
                startTmp.getLatitude(), startTmp.getLongitude());
        Node endNode = Node.createNode(-2L, endTmp.getStationName(), DirectionType.WALK, 
                endTmp.getLatitude(), endTmp.getLongitude());
        
        graph.put(startNode.getId(), new ArrayList<>());
        graph.put(endNode.getId(), new ArrayList<>());
        
        // 2. 출발지에서 인근 정류장으로 도보 연결
        List<Node> startCandidates = findNearbyNodes(nodes, startTmp.getLatitude(), startTmp.getLongitude(), ORIGIN_DESTINATION_WALK_DISTANCE);
        for (Node candidate : startCandidates) {
            Edge walkEdge = Edge.createEdge(-1L, startNode, candidate, DirectionType.WALK);
            graph.get(startNode.getId()).add(walkEdge);
            
            int weight = (int)(haversine(
                    startNode.getLatitude(), startNode.getLongitude(),
                    candidate.getLatitude(), candidate.getLongitude()
            ) * METER_CONVERSION);
            weightMap.put(Pair.of(startNode.getId(), candidate.getId()), weight);
        }
        
        // 3. 인근 정류장에서 도착지로 도보 연결
        List<Node> endCandidates = findNearbyNodes(nodes, endTmp.getLatitude(), endTmp.getLongitude(), ORIGIN_DESTINATION_WALK_DISTANCE);
        for (Node candidate : endCandidates) {
            Edge walkEdge = Edge.createEdge(-2L, candidate, endNode, DirectionType.WALK);
            
            if (!graph.containsKey(candidate.getId())) {
                graph.put(candidate.getId(), new ArrayList<>());
            }
            graph.get(candidate.getId()).add(walkEdge);
            
            int weight = (int)(haversine(
                    candidate.getLatitude(), candidate.getLongitude(),
                    endNode.getLatitude(), endNode.getLongitude()
            ) * METER_CONVERSION);
            weightMap.put(Pair.of(candidate.getId(), endNode.getId()), weight);
        }
        
        nodes.add(startNode);
        nodes.add(endNode);
    }

    private List<Node> findNearbyNodes(List<Node> nodes, double lat, double lon, int maxDistanceMeters) {
        return nodes.stream()
                .filter(node -> {
                    double distance = haversine(lat, lon, node.getLatitude(), node.getLongitude()) * METER_CONVERSION;
                    return distance <= maxDistanceMeters;
                })
                .sorted(Comparator.comparingDouble(node -> 
                        haversine(lat, lon, node.getLatitude(), node.getLongitude())))
                .limit(MAX_NEARBY_NODES)
                .collect(Collectors.toList());
    }

    private List<TransportationResponseDto.Step> runDijkstra(Map<Long, List<Edge>> graph, Node start, Node end, Map<Pair<Long, Long>, Integer> weightMap, List<Node> nodes) {
        // 1. 초기화
        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Edge> prevEdge = new HashMap<>();
        Map<Long, Node> prevNode = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        for (Node node : nodes) {
            distance.put(node.getId(), Integer.MAX_VALUE);
            prevNode.put(node.getId(), null);
            prevEdge.put(node.getId(), null);
        }
        distance.put(start.getId(), 0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> distance.get(n.getId())));
        pq.add(start);
        
        int visitedCount = 0;
        
        // 2. 다익스트라 알고리즘 실행
        while (!pq.isEmpty() && visitedCount < MAX_DIJKSTRA_VISITS) {
            Node curr = pq.poll();
            visitedCount++;
            
            if (visited.contains(curr.getId())) continue;
            visited.add(curr.getId());
            
            if (curr.equals(end)) break;

            List<Edge> currentEdges = graph.getOrDefault(curr.getId(), List.of());
            
            // 3. 동적 도보 연결 생성 (필요시)
            boolean hasUnvisitedDirectConnection = false;
            for (Edge edge : currentEdges) {
                if (edge == null || edge.getEndNode() == null) continue;
                Node neighbor = edge.getEndNode();
                if (!visited.contains(neighbor.getId())) {
                    hasUnvisitedDirectConnection = true;
                    break;
                }
            }
            
            if (!hasUnvisitedDirectConnection) {
                List<Node> nearbyNodes = findNearbyNodes(nodes, curr.getLatitude(), curr.getLongitude(), NEARBY_STATION_WALK_DISTANCE);
                for (Node nearbyNode : nearbyNodes) {
                    if (visited.contains(nearbyNode.getId())) continue;
                    
                    double walkDistance = haversine(
                            curr.getLatitude(), curr.getLongitude(),
                            nearbyNode.getLatitude(), nearbyNode.getLongitude()
                    ) * METER_CONVERSION;
                    
                    if (walkDistance <= NEARBY_STATION_WALK_DISTANCE) {
                        Edge walkEdge = Edge.createEdge(-3L, curr, nearbyNode, DirectionType.WALK);
                        currentEdges.add(walkEdge);
                        
                        int weight = (int)walkDistance + STEP_PENALTY;
                        int alt = distance.get(curr.getId()) + weight;
                        if (alt < distance.get(nearbyNode.getId())) {
                            distance.put(nearbyNode.getId(), alt);
                            prevNode.put(nearbyNode.getId(), curr);
                            prevEdge.put(nearbyNode.getId(), walkEdge);
                            pq.add(nearbyNode);
                        }
                    }
                }
            }
            
            // 4. 기존 엣지 처리
            for (Edge edge : currentEdges) {
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

                int weight = baseWeight;

                // 환승 패널티 적용
                Edge prevEdgeForCurr = prevEdge.get(curr.getId());
                if (prevEdgeForCurr != null &&
                        prevEdgeForCurr.getEdgeType() != DirectionType.WALK &&
                        edge.getEdgeType() != DirectionType.WALK) {
                    
                    if (prevEdgeForCurr.getEdgeType() != edge.getEdgeType()) {
                        weight += TRANSFER_PENALTY;
                    } else {
                        Route prevRoute = prevEdgeForCurr.getRoute();
                        Route currentRoute = edge.getRoute();
                        
                        if (prevRoute != null && currentRoute != null && 
                            !prevRoute.getRouteId().equals(currentRoute.getRouteId())) {
                            weight += TRANSFER_PENALTY;
                        }
                    }
                }

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

        // 5. 경로 역추적 및 steps 생성
        if (distance.get(end.getId()) == Integer.MAX_VALUE) {
            return new LinkedList<>();
        }

        List<Edge> pathEdges = new ArrayList<>();
        Node current = end;
        Set<Long> backtrackVisited = new HashSet<>();

        while (current != null && !current.equals(start)) {
            if (backtrackVisited.contains(current.getId())) break;
            backtrackVisited.add(current.getId());

            Edge edge = prevEdge.get(current.getId());
            if (edge == null) break;
            
            pathEdges.add(0, edge);
            current = prevNode.get(current.getId());
        }

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
            
            // 1. 연속된 같은 타입의 엣지들을 그룹화
            int j = i + 1;
            // 도보 처리
            if (currentType == DirectionType.WALK) {
                while (j < pathEdges.size() && pathEdges.get(j).getEdgeType() == DirectionType.WALK) {
                    j++;
                }
            } else {
                while (j < pathEdges.size()) {
                    Edge nextEdge = pathEdges.get(j);
                    if (nextEdge.getEdgeType() != currentType) break;
                    
                    Route currentRoute = currentEdge.getRoute();
                    Route nextRoute = nextEdge.getRoute();
                    
                    if ((currentRoute == null && nextRoute == null) ||
                        (currentRoute != null && nextRoute != null && 
                         currentRoute.getRouteId().equals(nextRoute.getRouteId()))) {
                        j++;
                    } else {
                        break;
                    }
                }
            }
            
            // 2. 노드명 및 기본 정보 설정
            String fromName = getNodeName(currentEdge.getStartNode());
            String toName = getNodeName(pathEdges.get(j - 1).getEndNode());
            
            if (currentType == DirectionType.WALK) {
                mergedSteps.add(new TransportationResponseDto.Step(
                    DirectionType.WALK, null, null, 0, null, null, fromName, toName
                ));
                i = j;
                continue;
            }
            
            // 3. 교통수단 상세 정보 (moveInfo) 설정
            List<TransportationResponseDto.MoveInfo> moveInfoList = createMoveInfoList(pathEdges, i, j);
            String routeName = getRouteName(pathEdges, i, j);
            // busInfo / subwayInfo 설정
            TransportationResponseDto.BusInfo busInfo = null;
            TransportationResponseDto.SubwayInfo subwayInfo = null;
            
            if (currentType == DirectionType.BUS) {
                try {
                    if (currentEdge.getStartNode() != null && currentEdge.getRoute() != null) {
                        busInfo = busInfoService.getBusInfo(
                            currentEdge.getStartNode().getStationName(), 
                            currentEdge.getRoute().getRouteId(), 
                            currentEdge.getStartNode().getLatitude(), 
                            currentEdge.getStartNode().getLongitude()
                        );
                        
                        if (busInfo != null && 
                            busInfo.isLowFloor() != null && !busInfo.isLowFloor().isEmpty() && 
                            busInfo.dispatchInterval() != null &&
                            busInfo.isLowFloor().stream().allMatch(floor -> !floor) &&
                            busInfo.dispatchInterval() == 0) {
                            return new ArrayList<>();
                        }
                    }
                        } catch (Exception e) {
                            log.error("버스 정보 조회 실패: {}", e.getMessage());
                        }
            } else if (currentType == DirectionType.SUBWAY) {
                try {
                    if (currentEdge.getStartNode() != null) {
                        TransportationResponseDto.NodeInfo nodeInfo = facilityService.getNodeInfo(currentEdge.getStartNode().getId(), currentEdge.getRoute().getRouteId());
                        
                        subwayInfo = new TransportationResponseDto.SubwayInfo(
                            nodeInfo.wheelchair(), 
                            nodeInfo.elevator(), 
                            nodeInfo.accessibleRestroom()
                        );
                    } else {
                        subwayInfo = new TransportationResponseDto.SubwayInfo(
                            new ArrayList<>(),
                            new ArrayList<>(),
                            false
                        );
                    }
                } catch (Exception e) {
                    log.error("지하철 정보 조회 실패: {}", e.getMessage());
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
    
    private String getNodeName(Node node) {
        return (node != null && node.getStationName() != null) ? node.getStationName() : "Unknown";
    }
    
    private List<TransportationResponseDto.MoveInfo> createMoveInfoList(List<Edge> pathEdges, int start, int end) {
        List<TransportationResponseDto.MoveInfo> moveInfoList = new ArrayList<>();
        for (int k = start + 1; k < end; k++) {
            Edge e = pathEdges.get(k);
            if (e.getStartNode() != null && e.getStartNode().getStationName() != null) {
                moveInfoList.add(new TransportationResponseDto.MoveInfo(e.getStartNode().getStationName()));
            }
        }
        return moveInfoList.isEmpty() ? null : moveInfoList;
    }
    
    private String getRouteName(List<Edge> pathEdges, int start, int end) {
        for (int k = start; k < end; k++) {
            Edge e = pathEdges.get(k);
            if (e.getRoute() != null && e.getRoute().getRouteName() != null) {
                return e.getRoute().getRouteName();
            }
        }
        return null;
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

    private Node findNearestNode(List<Node> nodes, double lat, double lon) {
        return nodes.stream()
                .min(Comparator.comparingDouble(n ->
                        haversine(lat, lon, n.getLatitude(), n.getLongitude())))
                .orElse(null);
    }

    private int calculateTransferCount(List<TransportationResponseDto.Step> steps) {
        int transferCount = 0;
        DirectionType previousMode = null;
        String previousRouteName = null;
        
        for (TransportationResponseDto.Step step : steps) {
            if (step.mode() != DirectionType.WALK && step.mode() != DirectionType.FROM_WAYPOINT && step.mode() != DirectionType.TO_WAYPOINT) {
                if (previousMode != null) {
                    if (previousMode == step.mode() && 
                        previousRouteName != null && step.routeName() != null &&
                        !previousRouteName.equals(step.routeName())) {
                        transferCount++;
                    } else if (previousMode != step.mode()) {
                        transferCount++;
                    }
                }
                previousMode = step.mode();
                previousRouteName = step.routeName();
            }
        }
        return transferCount;
    }
}
