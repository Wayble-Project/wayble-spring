package com.wayble.server.wayblezone.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.wayblezone.dto.WaybleZoneRegisterDto;
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
    @Builder.Default
    private double rating = 0.0; // 누적 평균 평점

    @Column(nullable = false, name = "review_count")
    @Builder.Default
    private int reviewCount = 0; // 리뷰 수

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0; // 즐겨찾기 수

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WaybleZoneImage> waybleZoneImageList = new ArrayList<>();

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviewList = new ArrayList<>();

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WaybleZoneOperatingHour> operatingHours = new ArrayList<>();

    @OneToOne(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private WaybleZoneFacility facility;

    @OneToMany(mappedBy = "waybleZone", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserPlaceWaybleZoneMapping> userPlaceMappings = new ArrayList<>();

    // 대표 이미지 필드 추가
    @Column(name = "main_image_url")
    private String mainImageUrl;

    // 혹시 필요할수도 있어서 추가해놓음
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public void addReviewCount(int count) {
        this.reviewCount += count;
    }

    public void addLikes(int count) {
        this.likes += count;
    }

    public static WaybleZone from(WaybleZoneRegisterDto dto) {
        return WaybleZone.builder()
                .zoneName(dto.zoneName())
                .contactNumber(dto.contactNumber())
                .zoneType(dto.waybleZoneType())
                .address(dto.address())
                .mainImageUrl(dto.thumbnailImageUrl())
                .rating(dto.averageRating() != null ? dto.averageRating() : 0.0)
                .reviewCount(dto.reviewCount())
                .likes(dto.likes())
                .build();
    }
}
