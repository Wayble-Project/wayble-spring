package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wayble_zone_facility") // 웨이블존 편의 시설 정보
@Access(AccessType.FIELD)
public class WaybleZoneFacility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 웨이블존 연관관계 (1:1로 가정 => 웨이블존당 시설 1세트)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    // 시설 정보 (있음/없음 여부)
    @Column(name = "has_slope", nullable = false)
    private boolean hasSlope; // 경사로

    @Column(name = "has_no_door_step", nullable = false)
    private boolean hasNoDoorStep; // 문턱

    @Column(name = "has_elevator", nullable = false)
    private boolean hasElevator; // 엘리베이터

    @Column(name = "has_table_seat", nullable = false)
    private boolean hasTableSeat; // 테이블석

    @Column(name = "has_disabled_toilet", nullable = false)
    private boolean hasDisabledToilet; // 장애인 화장실

    @Column(name = "floor_info", length = 20)
    private String floorInfo; // 층수 정보 (ex: "1층", "B1", "2층")
}