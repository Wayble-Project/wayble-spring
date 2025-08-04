package com.wayble.server.direction.entity.transportation;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

import com.wayble.server.direction.entity.DirectionType;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "node")
public class Node {
    @Id
    private Long id;

    @Column(name = "stationName", nullable = false)
    private String stationName;

    @Column(name = "nodeType", nullable = false)
    @Enumerated(EnumType.STRING)
    private DirectionType nodeType;

    @Column(name = "latitude", columnDefinition = "DECIMAL(10,7)", nullable = false)
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "DECIMAL(10,7)", nullable = false)
    private Double longitude;

    // 출발 edge 리스트 (정류장에서 출발)
    @OneToMany(mappedBy = "startNode")
    private List<Edge> outgoingEdges;

    // 도착 Edge 리스트 (정류장으로 도착)
    @OneToMany(mappedBy = "endNode")
    private List<Edge> incomingEdges;

    // 이 정류장이 기점/종점인 노선
    @OneToMany(mappedBy = "startNode")
    private List<Route> startRoutes;

    @OneToMany(mappedBy = "endNode")
    private List<Route> endRoutes;

    @OneToOne(mappedBy = "node")
    private Facility facility_id;

    public Node(Long id, String stationName, double latitude, double longitude) {
        this.id = id;
        this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id != null && id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
