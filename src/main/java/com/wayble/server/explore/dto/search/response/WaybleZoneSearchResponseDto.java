package com.wayble.server.explore.dto.search.response;

import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record WaybleZoneSearchResponseDto(

        Long zoneId,

        String zoneName,

        WaybleZoneType zoneType,

        String thumbnailImageUrl,

        Double distance,

        Double latitude,

        Double longitude,

        Double averageRating,

        Long reviewCount,

        FacilityResponseDto facility
) {
    public static WaybleZoneSearchResponseDto from(WaybleZoneDocument waybleZoneDocument, Double distance) {
        return WaybleZoneSearchResponseDto.builder()
                .zoneId(waybleZoneDocument.getZoneId())
                .zoneName(waybleZoneDocument.getZoneName())
                .zoneType(waybleZoneDocument.getZoneType())
                .thumbnailImageUrl(waybleZoneDocument.getThumbnailImageUrl())
                .averageRating(waybleZoneDocument.getAverageRating())
                .reviewCount(waybleZoneDocument.getReviewCount())
                .distance(distance)
                .latitude(waybleZoneDocument.getAddress().getLocation().getLat())
                .longitude(waybleZoneDocument.getAddress().getLocation().getLon())
                .facility(FacilityResponseDto.from(waybleZoneDocument.getFacility()))
                .build();
    }
}
