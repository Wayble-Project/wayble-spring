package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wayble_zone_operating_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wayble_zone_id", "day_of_week"})) // 웨이블존 영업 정보
public class WaybleZoneOperatingHour extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wayble_zone_id", nullable = false)
    private WaybleZone waybleZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; // 요일 정보

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 영업 시작 시간

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime; // 영업 종료 시간

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false; // 영업 여부

    @PrePersist
    @PreUpdate
    private void validateTimes() {
        if (isClosed) {
            // 휴무일에는 시간 정보가 있어서는 안 됨
            if (startTime != null || closeTime != null) {
                throw new IllegalStateException("휴무일에는 시간 정보가 있을 수 없습니다.");
            }
        } else {
            // 영업일에는 시간 정보가 필수
            if (startTime == null || closeTime == null) {
                throw new IllegalStateException("영업일에는 시작 시간과 종료 시간이 필요합니다.");
            }
            if (startTime.equals(closeTime)) {
                throw new IllegalStateException("시작 시간과 종료 시간이 동일할 수 없습니다.");
            }
        }
    }
}