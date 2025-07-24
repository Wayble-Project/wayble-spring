package com.wayble.server.direction.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.entity.WaybleMarker;
import com.wayble.server.direction.entity.type.Type;
import jakarta.annotation.PostConstruct;
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
    private Map<Long, Node> nodeMap;
    private Map<Long, Type> markerMap;
    private Map<Long, List<Edge>> adjacencyList;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 그래프
        InputStream graphStream = getClass().getResourceAsStream("/seocho_graph.json");
        JsonNode root = objectMapper.readTree(graphStream);
        nodes = Arrays.asList(objectMapper.convertValue(root.get("nodes"), Node[].class));
        edges = Arrays.asList(objectMapper.convertValue(root.get("edges"), Edge[].class));
        nodeMap = nodes.stream().collect(Collectors.toMap(
                node -> node.id, node -> node
        ));

        // 웨이블 마커 (임시)
        InputStream markerStream = getClass().getResourceAsStream("/wayble_markers.json");
        if (markerStream != null) {
            markers = objectMapper.readValue(markerStream, new TypeReference<>(){});
        } else {
            markers = new ArrayList<>();
        }

        markerMap = findWaybleMarkers();
        adjacencyList = new HashMap<>();

        for (Edge edge : edges) {
            boolean isWaybleMarker = markerMap.containsKey(edge.from) || markerMap.containsKey(edge.to);
            double distance = isWaybleMarker ? edge.length * 0.5 : edge.length;

            adjacencyList.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(
                    new Edge() {{
                        from = edge.from;
                        to = edge.to;
                        length = distance;
                    }}
            );
            adjacencyList.computeIfAbsent(edge.to, k -> new ArrayList<>()).add(
                    new Edge() {{
                        from = edge.to;
                        to = edge.from;
                        length = distance;
                    }}
            );
        }
    }

    private Map<Long, Type> findWaybleMarkers() {
        Map<Long, Type> waybleMarkers = new HashMap<>();

        for (WaybleMarker marker : markers) {
            long nearNode = nodes.stream()
                    .min(Comparator.comparingDouble(
                            n -> haversine(marker.lat, marker.lon, n.lat, n.lon)
                    ))
                    .map(node -> node.id)
                    .orElse(marker.id);

            if (nearNode != marker.id) {
                Type type = marker.type;
                waybleMarkers.put(nearNode, type);
            }
        }
        return waybleMarkers;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public Map<Long, List<Edge>> getGraph() {
        return adjacencyList;
    }

    public Map<Long, Node> getNodeMap() {
        return nodeMap;
    }

    public Map<Long, Type> getMarkerMap() {
        return markerMap;
    }
}
