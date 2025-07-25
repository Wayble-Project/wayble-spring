package com.wayble.server.explore.dto.search;

import com.wayble.server.explore.dto.FacilityResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneDistrictResponseDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType zoneType,
        String thumbnailImageUrl,
        Double latitude,
        Double longitude,
        Double averageRating,
        Long reviewCount,
        FacilityResponseDto facility,
        Long visitCount
) {
    public static WaybleZoneDistrictResponseDto from(WaybleZoneDocument document, Long visitCount) {
        return WaybleZoneDistrictResponseDto.builder()
                .zoneId(document.getZoneId())
                .zoneName(document.getZoneName())
                .zoneType(document.getZoneType())
                .thumbnailImageUrl(document.getThumbnailImageUrl())
                .latitude(document.getAddress().getLocation().getLat())
                .longitude(document.getAddress().getLocation().getLon())
                .averageRating(document.getAverageRating())
                .reviewCount(document.getReviewCount())
                .facility(FacilityResponseDto.from(document.getFacility()))
                .visitCount(visitCount != null ? visitCount : 0L)
                .build();
    }
}