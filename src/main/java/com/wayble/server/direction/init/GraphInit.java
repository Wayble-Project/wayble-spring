package com.wayble.server.direction.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.entity.WaybleMarker;
import com.wayble.server.direction.entity.type.Type;
import com.wayble.server.direction.exception.WalkingErrorCase;
import com.wayble.server.direction.service.util.HaversineUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GraphInit {

    private List<Node> nodes;
    private List<Edge> edges;
    private List<WaybleMarker> markers;
    private Map<Long, List<Edge>> adjacencyList;
    private Map<Long, Node> nodeMap;
    private Map<Long, Type> markerMap;

    @PostConstruct
    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 그래프
            try (InputStream graphStream = getClass().getResourceAsStream("/seocho_pedestrian.json")) {
                if (graphStream == null) {
                    throw new ApplicationException(WalkingErrorCase.GRAPH_FILE_NOT_FOUND);
                }
                JsonNode root = objectMapper.readTree(graphStream);
                nodes = Arrays.asList(objectMapper.convertValue(root.get("nodes"), Node[].class));
                edges = Arrays.asList(objectMapper.convertValue(root.get("edges"), Edge[].class));

                nodeMap = nodes.stream().collect(Collectors.toMap(Node::id, node -> node));
            }

            // 웨이블 마커
            try (InputStream markerStream = getClass().getResourceAsStream("/wayble_markers.json")) {
                markers = markerStream != null
                        ? objectMapper.readValue(markerStream, new TypeReference<>() {})
                        : new ArrayList<>();
            }
            markerMap = findWaybleMarkers();
            adjacencyList = buildAdjacencyList();
        } catch (IOException e) {
            log.error("🚨 그래프 초기화 실패: {}", e.getMessage());
            throw new ApplicationException(WalkingErrorCase.GRAPH_INIT_FAILED);
        }
    }

    private Map<Long, List<Edge>> buildAdjacencyList() {
        Map<Long, List<Edge>> adjacencyList = new HashMap<>();

        for (Edge edge : edges) {
            boolean isWaybleMarker = markerMap.containsKey(edge.from()) || markerMap.containsKey(edge.to());
            double distance = isWaybleMarker ? edge.length() * 0.5 : edge.length();

            // 양방향
            adjacencyList.computeIfAbsent(edge.from(), k -> new ArrayList<>())
                    .add(new Edge(edge.from(), edge.to(), distance, edge.geometry()));
            adjacencyList.computeIfAbsent(edge.to(), k -> new ArrayList<>())
                    .add(new Edge(edge.to(), edge.from(), distance, edge.geometry()));
        }
        return adjacencyList;
    }

    private Map<Long, Type> findWaybleMarkers() {
        Map<Long, Type> waybleMarkers = new HashMap<>();

        for (WaybleMarker marker : markers) {
            long nearNode = nodes.stream()
                    .min(Comparator.comparingDouble(
                            n -> HaversineUtil.haversine(marker.lat(), marker.lon(), n.lat(), n.lon())
                    ))
                    .map(Node::id)
                    .orElse(marker.id());

            if (nearNode != marker.id()) {
                waybleMarkers.put(nearNode, marker.type());
            }
        }
        return waybleMarkers;
    }

    public Map<Long, Node> getNodeMap() {
        return Collections.unmodifiableMap(nodeMap);
    }

    public Map<Long, Type> getMarkerMap() {
        return Collections.unmodifiableMap(markerMap);
    }

    public Map<Long, List<Edge>> getGraph() {
        return Collections.unmodifiableMap(adjacencyList);
    }
}
