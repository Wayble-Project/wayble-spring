package com.wayble.server.explore.dto.common;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneInfoResponseDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType zoneType,
        String thumbnailImageUrl,
        Double latitude,
        Double longitude,
        Double averageRating,
        Long reviewCount,
        FacilityResponseDto facility
) {
    public static WaybleZoneInfoResponseDto from(WaybleZoneDocument document) {
        return WaybleZoneInfoResponseDto.builder()
                .zoneId(document.getZoneId())
                .zoneName(document.getZoneName())
                .zoneType(document.getZoneType())
                .thumbnailImageUrl(document.getThumbnailImageUrl())
                .latitude(document.getAddress().getLocation().getLat())
                .longitude(document.getAddress().getLocation().getLon())
                .averageRating(document.getAverageRating())
                .reviewCount(document.getReviewCount())
                .facility(FacilityResponseDto.from(document.getFacility()))
                .build();
    }
}