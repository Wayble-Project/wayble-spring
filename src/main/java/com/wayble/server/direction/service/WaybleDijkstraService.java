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

    // 11cm의 오차 허용
    private static final double TOLERANCE = 0.000001;

    public WayblePathResponse createWayblePath(long start, long end) {
        List<Long> path = dijkstra(start, end);
        Map<Long, Type> markerMap = graphInit.getMarkerMap();

        int totalDistance = (int) Math.round(calculateDistance(path));
        // 노드 간 5초 대기 시간 추가 (횡단 보도, 보행자 상황 등 반영)
        int totalTime = (int) Math.round(calculateTime(path)) + path.size() * 5;

        List<WayblePathResponse.WayblePoint> wayblePoints = path.stream()
                .map(id -> {
                    Node node = graphInit.getNodeMap().get(id);
                    Type type = markerMap.getOrDefault(id, Type.NONE);
                    return new WayblePathResponse.WayblePoint(node.lat(), node.lon(), type);
                }).toList();

        List<double[]> polyline = createPolyLine(path);

        return WayblePathResponse.of(totalDistance, totalTime, wayblePoints, polyline);
    }

    private List<double[]> createPolyLine(List<Long> path) {
        List<double[]> polyline = new ArrayList<>();
        Map<Long, List<Edge>> adjacencyList = graphInit.getGraph();
        double[] last = null;

        for (int i = 0; i < path.size() - 1; i++) {
            long from = path.get(i);
            long to = path.get(i + 1);

            Edge edge = adjacencyList.getOrDefault(from, List.of()).stream()
                    .filter(e -> e.to() == to)
                    .findFirst()
                    .orElse(null);

            // 좌표 중복 제거 (동일 좌표가 연속될 시, 추가 X)
            if (edge != null && edge.geometry() != null) {
                for (double[] coord : edge.geometry()) {
                    if (last == null || isDifferent(last, coord)) {
                        polyline.add(coord);
                        last = coord;
                    }
                }
            } else {
                Node fromNode = graphInit.getNodeMap().get(from);
                Node toNode = graphInit.getNodeMap().get(to);

                double[] fromCoord = new double[]{fromNode.lon(), fromNode.lat()};
                double[] toCoord = new double[]{toNode.lon(), toNode.lat()};

                // 중복 확인 후, 중복 X일 때만 추가
                if (last == null || isDifferent(last, fromCoord)) {
                    polyline.add(fromCoord);
                }
                if (last == null || isDifferent(last, toCoord)) {
                    polyline.add(toCoord);
                    last = toCoord;
                }
            }
        }
        return polyline;
    }

    private double calculateTime(List<Long> path) {
        double averageSpeed = 1.0;
        double totalTime = 0.0;

        Map<Long, List<Edge>> adjacencyList = graphInit.getGraph();

        for (int i = 0; i < path.size() - 1; i++) {
            long from = path.get(i);
            long to = path.get(i + 1);

            Edge edge = adjacencyList.getOrDefault(from, List.of()).stream()
                    .filter(edge1 -> edge1.to() == to)
                    .findFirst()
                    .orElse(null);

            if (edge != null) {
                totalTime += edge.length() / averageSpeed;
            }
        }
        return totalTime;
    }

    private double calculateDistance(List<Long> path) {
        double totalDistance = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            long from = path.get(i);
            long to = path.get(i + 1);
            totalDistance += graphInit.getGraph().get(from).stream()
                    .filter(edge -> edge.to() == to)
                    .findFirst()
                    .map(Edge::length)
                    .orElse(0.0);
        }
        return totalDistance;
    }

    private List<Long> dijkstra(long start, long end) {
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(value -> value[1]));

        graphInit.getGraph().keySet().forEach(key ->
            dist.put(key, Double.POSITIVE_INFINITY)
        );
        dist.put(start, 0.0);
        pq.add(new double[]{start, 0.0});

        while (!pq.isEmpty()) {
            double[] current = pq.poll();
            long u = (long) current[0];
            if (u == end) break;

            for (Edge edge : graphInit.getGraph().getOrDefault(u, List.of())) {
                double alt = dist.get(u) + edge.length();
                if (alt < dist.get(edge.to())) {
                    dist.put(edge.to(), alt);
                    prev.put(edge.to(), u);
                    pq.add(new double[]{edge.to(), alt});
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

    private boolean isDifferent(double[] a, double[] b) {
        return Math.abs(a[0] - b[0]) > TOLERANCE || Math.abs(a[1] - b[1]) > TOLERANCE;
    }
}
