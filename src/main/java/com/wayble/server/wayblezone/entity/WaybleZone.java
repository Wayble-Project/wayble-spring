package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
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
@Table(name = "wayble_zone") // 웨이블 존
public class WaybleZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneName; // 가게 이름

    private String contactNumber; // 가게 전화 번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaybleZoneType zoneType; // 가게 타입 (음식점,카페,편의점)

    @Embedded
    private Address address; // 주소

    @Column(nullable = false)
    private double rating = 0.0; // 누적 평균 평점

    @Column(nullable = false)
    private int reviewCount = 0; // 리뷰 수

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaybleZoneImage> waybleZoneImageList = new ArrayList<>();

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaybleZoneOperatingHour> operatingHours = new ArrayList<>();

    @OneToOne(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private WaybleZoneFacility facility;

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPlaceWaybleZoneMapping> userPlaceMappings = new ArrayList<>();

    // 대표 이미지 필드 추가
    @Column(name = "main_image_url")
    private String mainImageUrl;

    // 혹시 필요할수도 있어서 추가해놓음
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }
}
