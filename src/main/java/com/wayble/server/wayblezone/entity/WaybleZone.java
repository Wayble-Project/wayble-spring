package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE wayble_zone SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "wayble_zone")
public class WaybleZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneName;

    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaybleZoneType zoneType;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaybleZoneImage> waybleZoneImageList = new ArrayList<>();

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    /**
     * TODO: 영업 시간 구현 필요
     */

    /**
     * TODO: 장애 시설 정보 구현 필요
     */

    /**
     * TODO: Review 관련 엔티티 구현 필요
     */

    /**
     * TODO: 내가 저장한 장소 관련 엔티티 구현 필요
     */
}
