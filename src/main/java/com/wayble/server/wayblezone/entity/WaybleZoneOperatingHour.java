package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wayble_zone_operating_hours") // 웨이블존 영업 정보
public class WaybleZoneOperatingHour extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek; // 요일 정보

    @Column(name = "start_time", nullable = false)
    private String startTime; // 영업 시작 시간

    @Column(name = "close_time", nullable = false)
    private String closeTime; // 영업 종료 시간
}