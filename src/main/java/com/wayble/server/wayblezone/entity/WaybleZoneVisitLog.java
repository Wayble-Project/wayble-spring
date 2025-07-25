package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.user.entity.Gender;
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
@Table(name = "user_place")
public class WaybleZoneVisitLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long zoneId;

    private Gender gender;

    private AgeGroup ageGroup;
}
