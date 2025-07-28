package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.user.entity.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wayble_zone_visit_log")
public class WaybleZoneVisitLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    private Gender gender;

    private AgeGroup ageGroup;

    private LocalDate visitedAt;

    public void updateVisitedAtToNow() {
        this.visitedAt = LocalDate.now();
    }
}
