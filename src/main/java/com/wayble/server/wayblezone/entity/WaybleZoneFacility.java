package com.wayble.server.wayblezone.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wayble_zone_facility")
public class WaybleZoneFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 웨이블존 연관관계 (1:1로 가정 => 웨이블존당 시설 1세트)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    // 시설 정보 (있음/없음 여부)
    @Column(nullable = false)
    private boolean hasSlope; // 경사로

    @Column(nullable = false)
    private boolean hasNoDoorStep; // 문턱

    @Column(nullable = false)
    private boolean hasElevator; // 엘리베이터

    @Column(nullable = false)
    private boolean hasTableSeat; // 테이블석

    @Column(nullable = false)
    private boolean hasDisabledToilet; // 장애인 화장실

    @Column(length = 20)
    private String floorInfo; // 층수 정보 (ex: "1층", "B1", "2층")
}