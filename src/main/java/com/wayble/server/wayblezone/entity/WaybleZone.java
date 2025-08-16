package com.wayble.server.wayblezone.entity;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneCreateDto;
import com.wayble.server.common.entity.Address;
import com.wayble.server.common.entity.BaseEntity;
import com.wayble.server.review.entity.Review;
import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.wayblezone.dto.WaybleZoneRegisterDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
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
    private long reviewCount = 0; // 리뷰 수

    @Column(nullable = false)
    @Builder.Default
    private long likes = 0; // 즐겨찾기 수

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

    // ES 동기화 추적 필드들
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt; // 마지막 수정 시간
    
    @Column(name = "synced_at")
    private LocalDateTime syncedAt; // ES와 마지막 동기화 시간

    // 혹시 필요할수도 있어서 추가해놓음
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
        this.markAsModified();
    }

    public void updateRating(double averageRating) {
        this.rating = averageRating;
        this.markAsModified();
    }

    public void addReviewCount(long count) {
        this.reviewCount += count;
        this.markAsModified();
    }

    public void addLikes(long count) {
        this.likes += count;
        if (this.likes < 0) this.likes = 0;
        this.markAsModified(); // 변경 시 자동으로 수정 시간 갱신
    }
    
    // 관리자 업데이트 메서드들
    public void updateZoneName(String zoneName) {
        this.zoneName = zoneName;
        this.markAsModified();
    }
    
    public void updateContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        this.markAsModified();
    }
    
    public void updateZoneType(WaybleZoneType zoneType) {
        this.zoneType = zoneType;
        this.markAsModified();
    }
    
    public void updateAddress(Address address) {
        this.address = address;
        this.markAsModified();
    }
    
    public void updateMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
        this.markAsModified();
    }
    
    // ES 동기화 관련 메서드들
    public void markAsModified() {
        this.lastModifiedAt = LocalDateTime.now();
    }
    
    public void markAsSynced() {
        this.syncedAt = LocalDateTime.now();
    }
    
    public boolean needsSync() {
        return syncedAt == null || 
               (lastModifiedAt != null && lastModifiedAt.isAfter(syncedAt));
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
                .lastModifiedAt(LocalDateTime.now())
                .syncedAt(null)
                .build();
    }

    public static WaybleZone fromAdminDto(AdminWaybleZoneCreateDto dto) {
        Address dtoAddress = Address
                .builder()
                .state(dto.state())
                .city(dto.city())
                .district(dto.district())
                .streetAddress(dto.streetAddress())
                .detailAddress(dto.detailAddress())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .build();

        return WaybleZone.builder()
                .zoneName(dto.zoneName())
                .contactNumber(dto.contactNumber())
                .zoneType(dto.zoneType())
                .address(dtoAddress)
                .mainImageUrl(dto.mainImageUrl())
                .rating(0.0)
                .reviewCount(0)
                .likes(0)
                .lastModifiedAt(LocalDateTime.now())
                .syncedAt(null)
                .build();
    }

    public static WaybleZone fromImporter(String zoneName, String phone, WaybleZoneType type, Address address) {
        return WaybleZone.builder()
                .zoneName(zoneName)
                .contactNumber(phone)
                .zoneType(type)
                .address(address)
                .rating(0.0)
                .reviewCount(0)
                .likes(0)
                .mainImageUrl(null)
                .lastModifiedAt(LocalDateTime.now())
                .syncedAt(null)
                .build();
    }
}
