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
    @Column(name = "has_slope", nullable = false, columnDefinition = "boolean default false")
    private boolean hasSlope = false; // 경사로

    @Column(name = "has_no_door_step", nullable = false, columnDefinition = "boolean default false")
    private boolean hasNoDoorStep = false; // 문턱

    @Column(name = "has_elevator", nullable = false, columnDefinition = "boolean default false")
    private boolean hasElevator = false; // 엘리베이터

    @Column(name = "has_table_seat", nullable = false, columnDefinition = "boolean default false")
    private boolean hasTableSeat = false; // 테이블석

    @Column(name = "has_disabled_toilet", nullable = false, columnDefinition = "boolean default false")
    private boolean hasDisabledToilet = false; // 장애인 화장실

    @Column(name = "floor_info", length = 20)
    private String floorInfo; // 층수 정보 (ex: "1층", "B1", "2층")
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasSlope) sb.append("경사로 ");
        if (hasNoDoorStep) sb.append("문턱없음 ");
        if (hasElevator) sb.append("엘리베이터 ");
        if (hasTableSeat) sb.append("테이블석 ");
        if (hasDisabledToilet) sb.append("장애인화장실 ");
        if (floorInfo != null) sb.append(floorInfo).append(" ");
        
        return sb.length() > 0 ? sb.toString().trim() : "시설정보 없음";
    }
}