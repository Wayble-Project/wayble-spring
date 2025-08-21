package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.dto.InternalStep;
import com.wayble.server.direction.dto.TransportationGraphDto;
import com.wayble.server.direction.dto.request.TransportationRequestDto;
import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.transportation.Edge;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.entity.transportation.Route;
import com.wayble.server.direction.entity.type.DirectionType;
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
    
    // 공간 필터링
    private static final double SPATIAL_BUFFER_KM = 15.0; // 지작점/도착점 주변 15km

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
        List<List<InternalStep>> allRoutes = findMultipleTransportationRoutes(start, end);

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
        List<List<InternalStep>> pagedRoutes = allRoutes.subList(startIndex, endIndex);
        for (int i = 0; i < pagedRoutes.size(); i++) {
            List<InternalStep> internalRoute = pagedRoutes.get(i);
            
            // InternalStep을 TransportationResponseDto.Step으로 변환 (API 호출 포함)
            List<TransportationResponseDto.Step> enrichedRoute = enrichRoutesWithServiceInfo(internalRoute);
            
            TransportationResponseDto.Route routeObj = createRoute(enrichedRoute, startIndex + i + 1);
            routeList.add(routeObj);
        }

        return new TransportationResponseDto(routeList, pageInfo);
    }

    private TransportationResponseDto.Route createRoute(List<TransportationResponseDto.Step> steps, int routeIndex) {
        return new TransportationResponseDto.Route(routeIndex, steps);
    }

    private List<List<InternalStep>> findMultipleTransportationRoutes(Node startTmp, Node endTmp){
        List<Node> nodes = null;
        List<Edge> edges = null;
        
        try {
            // 1. 공간 필터링을 사용한 데이터 로드
            double[] boundingBox = calculateBoundingBox(startTmp, endTmp);
            List<Object[]> nodeData = nodeRepository.findNodesInBoundingBox(
                boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]
            );
            
            // Object[]를 Node 객체로 변환
            nodes = nodeData.stream()
                .map(data -> new Node(
                    (Long) data[0],           // id
                    (String) data[1],         // stationName
                    (DirectionType) data[2],  // nodeType
                    (Double) data[3],         // latitude
                    (Double) data[4]          // longitude
                ))
                .collect(Collectors.toList());
            // 최적화된 쿼리 사용: 필요한 컬럼만 조회
            List<Object[]> edgeData = edgeRepository.findEdgesInBoundingBox(
                boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]
            );
            
            // Object[]를 Edge 객체로 변환
            edges = edgeData.stream()
                .map(data -> {
                    // DirectionType 객체 직접 캐스팅
                    DirectionType edgeType = (DirectionType) data[3];
                    
                    // Node 객체 생성
                    Node startNode = Node.createNode(
                        (Long) data[1],           // startNode.id
                        (String) data[4],         // startNode.stationName
                        edgeType,                 // edgeType
                        (Double) data[5],         // startNode.latitude
                        (Double) data[6]          // startNode.longitude
                    );
                    
                    Node endNode = Node.createNode(
                        (Long) data[2],           // endNode.id
                        (String) data[7],         // endNode.stationName
                        edgeType,                 // edgeType
                        (Double) data[8],         // endNode.latitude
                        (Double) data[9]          // endNode.longitude
                    );
                    
                    // Route 객체 생성 (null일 수 있음)
                    Route route = null;
                    if (data[10] != null) { // routeId가 null이 아닌 경우
                        route = Route.createRoute(
                            (Long) data[10],      // routeId
                            (String) data[11],    // routeName
                            edgeType,             // routeType
                            startNode,
                            endNode
                        );
                    }
                    
                    // Edge 객체 생성
                    return Edge.createEdgeWithRoute(
                        (Long) data[0],           // edge.id
                        startNode,
                        endNode,
                        edgeType,                 // edgeType
                        route
                    );
                })
                .collect(Collectors.toList());
            
            log.debug("Spatial filtering loaded {} nodes and {} edges", nodes.size(), edges.size());
            
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
            List<List<InternalStep>> result = findMultipleOptimalRoutes(
                graphData.graph(), startTmp, endTmp, graphData.weightMap(), nodes, nearestToStart, nearestToEnd
            );
            
            
            return result;
        } catch (OutOfMemoryError e) {
            log.error("Out of memory error in transportation route finding: {}", e.getMessage());
            throw new ApplicationException(PATH_NOT_FOUND);
        } finally {
            // 5. 메모리 정리 (finally 블록에서 확실히 실행)
            if (nodes != null) {
                nodes.clear();
                nodes = null;
            }
            if (edges != null) {
                edges.clear();
                edges = null;
            }
            
            // 명시적 GC 호출
            if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory() * 0.1) {
                System.gc();
            }
        }
    }
    
    private double[] calculateBoundingBox(Node start, Node end) {
        double minLat = Math.min(start.getLatitude(), end.getLatitude()) - SPATIAL_BUFFER_KM / 111.0;
        double maxLat = Math.max(start.getLatitude(), end.getLatitude()) + SPATIAL_BUFFER_KM / 111.0;
        double minLon = Math.min(start.getLongitude(), end.getLongitude()) - SPATIAL_BUFFER_KM / (111.0 * Math.cos(Math.toRadians(start.getLatitude())));
        double maxLon = Math.max(start.getLongitude(), end.getLongitude()) + SPATIAL_BUFFER_KM / (111.0 * Math.cos(Math.toRadians(start.getLatitude())));
        
        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    private List<List<InternalStep>> findMultipleOptimalRoutes(
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
        List<List<InternalStep>> allRoutes = findMultipleRoutes(graph, startNode, endNode, weightMap, nodes);
        
        // 3. 경로 필터링 및 정렬
        List<List<InternalStep>> result = filterAndSortRoutes(allRoutes);
        return result;
    }

    private List<List<InternalStep>> findMultipleRoutes(
            Map<Long, List<Edge>> graph, 
            Node start, 
            Node end, 
            Map<Pair<Long, Long>, Integer> weightMap, 
            List<Node> nodes) {
        
        List<List<InternalStep>> routes = new ArrayList<>();
        
        // 1. 기본 다익스트라로 첫 번째 경로 찾기
        List<InternalStep> firstRoute = runDijkstra(graph, start, end, weightMap, nodes);
        if (!firstRoute.isEmpty()) {
            routes.add(firstRoute);
        }
        
        // 2. 효율적인 다중 경로 찾기 - 한 번의 탐색으로 여러 경로 생성
        if (!firstRoute.isEmpty()) {
            List<List<InternalStep>> alternativeRoutes = findAlternativeRoutesEfficiently(
                graph, start, end, weightMap, nodes, firstRoute
            );
            routes.addAll(alternativeRoutes);
        }
        
        return routes;
    }

    private List<List<InternalStep>> findAlternativeRoutesEfficiently(
            Map<Long, List<Edge>> graph, 
            Node start, 
            Node end, 
            Map<Pair<Long, Long>, Integer> weightMap, 
            List<Node> nodes,
            List<InternalStep> firstRoute) {
        
        List<List<InternalStep>> alternativeRoutes = new ArrayList<>();
        
        // 첫 번째 경로에서 실제 사용된 엣지들을 추출
        Set<Pair<Long, Long>> usedEdges = extractActualEdgesFromRoute(firstRoute, graph);
        
        // 최대 4개의 추가 경로 찾기
        for (int i = 0; i < 4 && alternativeRoutes.size() < MAX_ROUTES - 1; i++) {
            // 실제 사용된 엣지들에만 패널티를 적용한 가중치 맵 생성
            Map<Pair<Long, Long>, Integer> penalizedWeightMap = createActualEdgePenalizedWeightMap(weightMap, usedEdges, i + 1);
            
            // 다익스트라로 새로운 경로 찾기
            List<InternalStep> newRoute = runDijkstra(graph, start, end, penalizedWeightMap, nodes);
            
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





    private Set<Pair<Long, Long>> extractActualEdgesFromRoute(List<InternalStep> route, Map<Long, List<Edge>> graph) {
        Set<Pair<Long, Long>> usedEdges = new HashSet<>();
        
        for (InternalStep step : route) {
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
    
    private boolean areRoutesIdentical(List<InternalStep> route1, List<InternalStep> route2) {
        // 두 경로가 완전히 동일한지 확인
        if (route1.size() != route2.size()) {
            return false;
        }
        
        for (int i = 0; i < route1.size(); i++) {
            InternalStep step1 = route1.get(i);
            InternalStep step2 = route2.get(i);
            
            if (step1.mode() != step2.mode() || 
                !Objects.equals(step1.from(), step2.from()) || 
                !Objects.equals(step1.to(), step2.to()) ||
                !Objects.equals(step1.routeName(), step2.routeName())) {
                return false;
            }
        }
        
        return true;
    }

    private List<List<InternalStep>> filterAndSortRoutes(List<List<InternalStep>> routes) {
        List<List<InternalStep>> filteredRoutes = routes.stream()
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
                        .<List<InternalStep>>comparingInt(this::calculateTransferCount)
                        .thenComparingInt(this::calculateWalkDistance))
                .limit(MAX_ROUTES)
                .collect(Collectors.toList());
        
        return filteredRoutes;
    }

    private int calculateWalkDistance(List<InternalStep> route) {
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
        
        // 1. 노드 ID를 Set으로 변환해 빠른 검색
        Set<Long> nodeIds = nodes.stream()
                .map(Node::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 2. 노드 초기화
        for (Long nodeId : nodeIds) {
            graph.put(nodeId, new ArrayList<>());
        }
        
        // 3. 기존 엣지 추가 및 가중치 계산 (필터링된 노드만)
        for (Edge edge : edges) {
            if (edge == null) continue;

            Node start = edge.getStartNode();
            Node end = edge.getEndNode();
            if (start == null || end == null || start.getId() == null || end.getId() == null) continue;

            Long startId = start.getId();
            Long endId = end.getId();
            
            // 공간 필터링된 노드들만 처리
            if (!nodeIds.contains(startId) || !nodeIds.contains(endId)) continue;

            graph.get(startId).add(edge);
            
            int weight = (int)(haversine(
                    start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude()
            ) * METER_CONVERSION);
            weightMap.put(Pair.of(startId, endId), weight);
        }
        
        // 4. 출발지/도착지 도보 연결 추가
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
        // 대략적인 거리 필터링
        double maxDistanceKm = maxDistanceMeters / 1000.0;
        
        return nodes.stream()
                .filter(node -> {
                    // 빠른 거리 계산 (대략적)
                    double latDiff = Math.abs(lat - node.getLatitude());
                    double lonDiff = Math.abs(lon - node.getLongitude());
                    
                    // 필터링 (1도 ≈ 111km)
                    if (latDiff > maxDistanceKm / 111.0 || lonDiff > maxDistanceKm / (111.0 * Math.cos(Math.toRadians(lat)))) {
                        return false;
                    }
                    
                    // 정확한 거리 계산
                    double distance = haversine(lat, lon, node.getLatitude(), node.getLongitude()) * METER_CONVERSION;
                    return distance <= maxDistanceMeters;
                })
                .sorted(Comparator.comparingDouble(node -> 
                        haversine(lat, lon, node.getLatitude(), node.getLongitude())))
                .limit(MAX_NEARBY_NODES)
                .collect(Collectors.toList());
    }

    private List<InternalStep> runDijkstra(Map<Long, List<Edge>> graph, Node start, Node end, Map<Pair<Long, Long>, Integer> weightMap, List<Node> nodes) {
        // 1. 초기화 - HashMap 대신 Array 사용으로 성능 향상
        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Edge> prevEdge = new HashMap<>();
        Map<Long, Node> prevNode = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        Map<Long, Node> nodeMap = nodes.stream()
                .collect(Collectors.toMap(
                    Node::getId, 
                    node -> node,
                    (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        for (Node node : nodes) {
            distance.put(node.getId(), Integer.MAX_VALUE);
            prevNode.put(node.getId(), null);
            prevEdge.put(node.getId(), null);
        }
        distance.put(start.getId(), 0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Math.min(1000, nodes.size()), 
                Comparator.comparingInt(n -> distance.get(n.getId())));
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
        
        long requestId = System.currentTimeMillis();


        while (current != null && !current.equals(start)) {
            if (backtrackVisited.contains(current.getId())) break;
            backtrackVisited.add(current.getId());

            Edge edge = prevEdge.get(current.getId());
            if (edge == null) break;
            
            pathEdges.add(0, edge);
            current = prevNode.get(current.getId());
        }

        return mergeConsecutiveRoutes(pathEdges, requestId);
    }
    
    private List<TransportationResponseDto.Step> enrichRoutesWithServiceInfo(List<InternalStep> steps) {
        List<TransportationResponseDto.Step> enrichedSteps = new ArrayList<>();
        
        for (InternalStep step : steps) {
                TransportationResponseDto.BusInfo busInfo = null;
                TransportationResponseDto.SubwayInfo subwayInfo = null;
                
                if (step.mode() == DirectionType.BUS) {
                    log.info("🚌 최종 경로 - 버스 정보 조회: from={}, to={}, routeId={}", step.from(), step.to(), step.routeId());
                    
                    if (step.routeId() != null && step.startNode() != null) {
                        try {
                            busInfo = busInfoService.getBusInfo(
                                step.from(),
                                step.routeId(),
                                step.startNode().getLatitude(),
                                step.startNode().getLongitude()
                            );
                        } catch (Exception e) {
                            log.error("버스 정보 조회 실패: {}", e.getMessage());
                        }
                    }
                } else if (step.mode() == DirectionType.SUBWAY) {
                    log.info("🚇 최종 경로 - 지하철 정보 조회: from={}, to={}, routeId={}", step.from(), step.to(), step.routeId());
                    if (step.routeId() != null && step.startNode() != null) {
                        try {
                            TransportationResponseDto.NodeInfo nodeInfo = facilityService.getNodeInfo(step.startNode().getId(), step.routeId());
                            subwayInfo = new TransportationResponseDto.SubwayInfo(
                                nodeInfo.wheelchair(),
                                nodeInfo.elevator(),
                                nodeInfo.accessibleRestroom()
                            );
                        } catch (Exception e) {
                            log.error("지하철 정보 조회 실패: {}", e.getMessage());
                            subwayInfo = new TransportationResponseDto.SubwayInfo(
                                new ArrayList<>(),
                                new ArrayList<>(),
                                false
                            );
                        }
                    }
                }
                
                // 새로운 Step 생성 (busInfo, subwayInfo 포함)
                TransportationResponseDto.Step enrichedStep = new TransportationResponseDto.Step(
                    step.mode(),
                    step.moveInfo(),
                    step.routeName(),
                    step.moveNumber(),
                    busInfo,
                    subwayInfo,
                    step.from(),
                    step.to()
                );
                enrichedSteps.add(enrichedStep);
            }
            
            return enrichedSteps;
    }

    private List<InternalStep> mergeConsecutiveRoutes(List<Edge> pathEdges, long requestId) {
        List<InternalStep> mergedSteps = new ArrayList<>();
        
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
                int walkDistance = 0; // 미터 단위
                Node walkStartNode = pathEdges.get(i).getStartNode();
                Node walkEndNode = pathEdges.get(j - 1).getEndNode();
                
                if (walkStartNode != null && walkEndNode != null) {
                    double distanceKm = haversine(
                        walkStartNode.getLatitude(), walkStartNode.getLongitude(),
                        walkEndNode.getLatitude(), walkEndNode.getLongitude()
                    );
                    walkDistance = (int) (distanceKm * 1000); // km를 m로 변환
                }
                
                mergedSteps.add(new InternalStep(
                    DirectionType.WALK, null, null, walkDistance, null, null, fromName, toName, null, walkStartNode, walkEndNode
                ));
                i = j;
                continue;
            }
            
            // 3. 교통수단 상세 정보 (moveInfo) 설정
            List<TransportationResponseDto.MoveInfo> moveInfoList = createMoveInfoList(pathEdges, i, j);
            // busInfo / subwayInfo는 나중에 설정 (최종 경로 선택 후)
            TransportationResponseDto.BusInfo busInfo = null;
            TransportationResponseDto.SubwayInfo subwayInfo = null;

            int moveNumber = j - i - 1;
            
            String routeName = getRouteName(pathEdges, i, j);

            // routeId와 Node 정보 추출
            Long routeId = null;
            Node startNode = currentEdge.getStartNode();
            Node endNode = pathEdges.get(j - 1).getEndNode();
            
            if (currentEdge.getRoute() != null) {
                routeId = currentEdge.getRoute().getRouteId();
            }
            
            mergedSteps.add(new InternalStep(
                currentType,
                moveInfoList,
                routeName,
                moveNumber,
                busInfo,
                subwayInfo,
                fromName,
                toName,
                routeId,
                startNode,
                endNode
            ));
            
            i = j;
        }
        
        // 환승 시 walk step 추가
        return addTransferWalkSteps(mergedSteps, pathEdges);
    }
    
    private List<InternalStep> addTransferWalkSteps(List<InternalStep> steps, List<Edge> pathEdges) {
        List<InternalStep> result = new ArrayList<>();
        
        for (int i = 0; i < steps.size(); i++) {
            InternalStep currentStep = steps.get(i);
            result.add(currentStep);
            
            // 마지막 step이 아니고, 현재 step이 walk가 아닌 경우
            if (i < steps.size() - 1 && currentStep.mode() != DirectionType.WALK) {
                InternalStep nextStep = steps.get(i + 1);
                
                // 다음 step도 walk가 아닌 경우 (bus -> subway, subway -> bus 등)
                if (nextStep.mode() != DirectionType.WALK) {
                    // 환승 walk step 추가
                    String transferFrom = currentStep.to();
                    String transferTo = nextStep.from();
                    
                    // 이전 step의 도착지와 다음 step의 출발지 사이의 직선거리 계산
                    int walkDistance = calculateTransferWalkDistance(transferFrom, transferTo, pathEdges);
                    
                    InternalStep walkStep = new InternalStep(
                        DirectionType.WALK,
                        null,
                        null,
                        walkDistance,
                        null,
                        null,
                        transferFrom,
                        transferTo,
                        null,
                        null,
                        null
                    );
                    
                    result.add(walkStep);
                }
            }
        }
        
        return result;
    }
    
    private int calculateTransferWalkDistance(String fromStation, String toStation, List<Edge> pathEdges) {
        // pathEdges에서 해당 정류장의 노드 정보 찾기
        Node fromNode = null;
        Node toNode = null;
        
        for (Edge edge : pathEdges) {
            if (edge.getStartNode() != null && edge.getStartNode().getStationName() != null && 
                edge.getStartNode().getStationName().equals(fromStation)) {
                fromNode = edge.getStartNode();
            }
            if (edge.getEndNode() != null && edge.getEndNode().getStationName() != null && 
                edge.getEndNode().getStationName().equals(fromStation)) {
                fromNode = edge.getEndNode();
            }
            if (edge.getStartNode() != null && edge.getStartNode().getStationName() != null && 
                edge.getStartNode().getStationName().equals(toStation)) {
                toNode = edge.getStartNode();
            }
            if (edge.getEndNode() != null && edge.getEndNode().getStationName() != null && 
                edge.getEndNode().getStationName().equals(toStation)) {
                toNode = edge.getEndNode();
            }
        }
        
        if (fromNode != null && toNode != null) {
            double distanceKm = haversine(
                fromNode.getLatitude(), fromNode.getLongitude(),
                toNode.getLatitude(), toNode.getLongitude()
            );
            return (int) (distanceKm * 1000); // km를 m로 변환
        }
        
        return 0; // 노드를 찾지 못한 경우
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

    private int calculateTransferCount(List<InternalStep> steps) {
        int transferCount = 0;
        DirectionType previousMode = null;
        String previousRouteName = null;
        
        for (InternalStep step : steps) {
            if (step.mode() != DirectionType.WALK && step.mode() != DirectionType.FROM_WAYPOINT && step.mode() != DirectionType.TO_WAYPOINT) {
                if (previousMode != null) {
                    if (previousMode == step.mode() && 
                        previousRouteName != null && step.routeName() != null &&
                        !previousRouteName.equals(step.routeName())) {
                        transferCount++;
                    } else if (previousMode == step.mode() && 
                        previousRouteName != null && step.routeName() != null &&
                        previousRouteName.equals(step.routeName())) {
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
