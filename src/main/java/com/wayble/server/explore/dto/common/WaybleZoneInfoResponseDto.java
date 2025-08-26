package com.wayble.server.explore.dto.common;

import com.wayble.server.explore.entity.EsWaybleZoneFacility;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneInfoResponseDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType zoneType,
        String thumbnailImageUrl,
        String address,
        Double latitude,
        Double longitude,
        Double averageRating,
        long reviewCount,
        FacilityResponseDto facility
) {
    public static WaybleZoneInfoResponseDto from(WaybleZoneDocument document) {
        return WaybleZoneInfoResponseDto.builder()
                .zoneId(document.getZoneId())
                .zoneName(document.getZoneName())
                .zoneType(document.getZoneType())
                .thumbnailImageUrl(document.getThumbnailImageUrl())
                .address(document.getAddress().toFullAddress())
                .latitude(document.getAddress().getLocation().getLat())
                .longitude(document.getAddress().getLocation().getLon())
                .averageRating(document.getAverageRating())
                .reviewCount(document.getReviewCount())
                .facility(FacilityResponseDto.from(document.getFacility()))
                .build();
    }
    
    public static WaybleZoneInfoResponseDto fromEntity(WaybleZone waybleZone) {
        return WaybleZoneInfoResponseDto.builder()
                .zoneId(waybleZone.getId())
                .zoneName(waybleZone.getZoneName())
                .zoneType(waybleZone.getZoneType())
                .thumbnailImageUrl(waybleZone.getMainImageUrl())
                .address(waybleZone.getAddress().toFullAddress())
                .latitude(waybleZone.getAddress().getLatitude())
                .longitude(waybleZone.getAddress().getLongitude())
                .averageRating(waybleZone.getRating())
                .reviewCount(waybleZone.getReviewCount())
                .facility(waybleZone.getFacility() != null ? 
                        FacilityResponseDto.from(EsWaybleZoneFacility.from(waybleZone.getFacility())) : null)
                .build();
    }
}