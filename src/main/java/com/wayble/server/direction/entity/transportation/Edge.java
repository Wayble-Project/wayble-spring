package com.wayble.server.direction.entity.transportation;

import com.wayble.server.direction.entity.DirectionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "edge")
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "edgeType", nullable = false)
    @Enumerated(EnumType.STRING)
    private DirectionType edgeType;

    // 출발 노드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_node_id")
    private Node startNode;

    // 도착 노드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_node_id")
    private Node endNode;

    // 해당 연결이 속한 노선
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    public static Edge createEdge(Long id, Node startNode, Node endNode, DirectionType edgeType) {
        return Edge.builder()
                .id(id)
                .edgeType(edgeType)
                .startNode(startNode)
                .endNode(endNode)
                .route(null)
                .build();
    }
}
