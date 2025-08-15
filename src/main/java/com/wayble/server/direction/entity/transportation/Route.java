package com.wayble.server.direction.entity.transportation;

import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.transportation.*;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route")
public class Route {

    @Id
    private Long routeId;

    @Column(name = "routeName", nullable = false)
    private String routeName; //노선명(번호/호선)

    @Column(name = "routeType", nullable = false)
    @Enumerated(EnumType.STRING)
    private DirectionType routeType; // SUBWAY, BUS

    // 기점 정류장
    @ManyToOne
    @JoinColumn(name = "start_node_id")
    private Node startNode;

    // 종점 정류장
    @ManyToOne
    @JoinColumn(name = "end_node_id")
    private Node endNode;
    
    // 휠체어 정보
    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private List<Wheelchair> wheelchairs;
}
