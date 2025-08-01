package com.wayble.server.admin.dto.wayblezone;

import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminWaybleZoneDetailDto(
        Long zoneId,
        String zoneName,
        String contactNumber,
        WaybleZoneType zoneType,
        String fullAddress,
        Double latitude,
        Double longitude,
        double rating,
        long reviewCount,
        long likes,
        String mainImageUrl,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        LocalDateTime syncedAt,
        
        // 시설 정보
        FacilityInfo facility,
        
        // 운영시간 정보
        List<OperatingHourInfo> operatingHours,
        
        // 이미지 목록
        List<String> imageUrls,
        
        // 최근 리뷰 (최대 5개)
        List<RecentReviewInfo> recentReviews
) {
    
    public record FacilityInfo(
            WaybleZoneFacility facilityType,
            String description
    ) {}
    
    public record OperatingHourInfo(
            String dayOfWeek,
            String openTime,
            String closeTime,
            boolean isHoliday
    ) {}
    
    public record RecentReviewInfo(
            Long reviewId,
            String content,
            double rating,
            String userName,
            LocalDateTime createdAt
    ) {}
}