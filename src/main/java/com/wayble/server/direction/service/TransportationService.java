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
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransportationService {
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    public TransportationResponseDto findRoutes(TransportationRequestDto request){

        TransportationRequestDto.Location origin = request.origin();
        TransportationRequestDto.Location destination = request.origin();

        Node start = new Node(origin.name(), origin.latitude(), origin.longitude());
        Node end = new Node(destination.name(), destination.latitude(), destination.longitude());

        List<TransportationResponseDto.Step> steps = dijstra(start, end);


        int startIndex = (request.cursor() != null) ? request.cursor() : 0;
        int pageSize = request.size()    != null ? request.size()    : steps.size();
        int endIndex = Math.min(startIndex + pageSize, steps.size());
        boolean hasNext = endIndex < steps.size();
        Integer nextCursor = hasNext ? endIndex : null;
        TransportationResponseDto.PageInfo pageInfo = new TransportationResponseDto.PageInfo(nextCursor, hasNext);

        return new TransportationResponseDto(steps, pageInfo);
    }

    private List<TransportationResponseDto.Step> dijstra(Node start, Node end){
        List<Node> nodes = nodeRepository.findAll();
        List<Edge> edges = edgeRepository.findAll();

        Map<Node, Integer> distance = new HashMap<>();
        Map<Node, Edge> prevEdge = new HashMap<>();
        Map<Node, Node> prevNode = new HashMap<>();
        for (Node node : nodes) distance.put(node, Integer.MAX_VALUE);
        distance.put(start, 0);

        // 그래프 구성: 각 노드에서 출발 가능한 엣지 목록
        Map<Node, List<Edge>> graph = new HashMap<>();
        for (Node node : nodes) graph.put(node, new ArrayList<>());
        for (Edge edge : edges) {
            graph.get(edge.getStartNode()).add(edge);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(distance::get));
        pq.add(start);

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            if (curr.equals(end)) break;
            for (Edge edge : graph.getOrDefault(curr, List.of())) {
                Node neighbor = edge.getEndNode();
                int alt = distance.get(curr);
                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor, alt);
                    prevNode.put(neighbor, curr);
                    prevEdge.put(neighbor, edge);
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

    private Map<Node, List<Edge>> buildGraph(List<Node> nodes, List<Edge> edges) {
        Map<Node, List<Edge>> graph = new HashMap<>();
        for (Node node : nodes) graph.put(node, new ArrayList<>());
        for (Edge edge : edges) graph.get(edge.getStartNode()).add(edge);
        return graph;
    }
}
