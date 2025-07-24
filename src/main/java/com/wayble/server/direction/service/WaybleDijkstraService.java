package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.response.WayblePathResponse;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.entity.type.Type;
import com.wayble.server.direction.init.GraphInit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WaybleDijkstraService {

    private final GraphInit graphInit;

    public WayblePathResponse findWayblePath(long start, long end) {
        List<Long> path = dijkstra(start, end);
        Map<Long, Type> markerMap = graphInit.getMarkerMap();

        List<WayblePathResponse.WayblePoint> wayblePoints = path.stream()
                .map(id -> {
                    Node node = graphInit.getNodeMap().get(id);
                    Type type = markerMap.getOrDefault(id, Type.NONE);
                    return new WayblePathResponse.WayblePoint(id, node.lat, node.lon, type);
                }).toList();

        double totalDistance = calculateDistance(path);
        return WayblePathResponse.of(totalDistance, wayblePoints);
    }

    private List<Long> dijkstra(long start, long end) {
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingDouble(value -> value[1]));

        graphInit.getGraph().keySet().forEach(key -> {
            dist.put(key, Double.POSITIVE_INFINITY);
        });
        dist.put(start, 0.0);
        pq.add(new long[]{start, 0});

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            long u = current[0];
            if (u == end) break;

            for (Edge edge : graphInit.getGraph().getOrDefault(u, List.of())) {
                double alt = dist.get(u) + edge.length;
                if (alt < dist.get(edge.to)) {
                    dist.put(edge.to, alt);
                    prev.put(edge.to, u);
                    pq.add(new long[]{edge.to, (long) alt});
                }
            }
        }
        List<Long> path = new ArrayList<>();

        for (Long at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private double calculateDistance(List<Long> path) {
        double totalDistance = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            long from = path.get(i);
            long to = path.get(i + 1);
            totalDistance += graphInit.getGraph().get(from).stream()
                    .filter(edge -> edge.to == to)
                    .findFirst()
                    .map(edge -> edge.length)
                    .orElse(0.0);
        }
        return totalDistance;
    }
}
