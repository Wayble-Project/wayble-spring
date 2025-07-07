package com.wayble.server.search.dto;

import com.wayble.server.search.entity.WaybleZoneDocument;
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

        Long reviewCount
) {
    public static WaybleZoneSearchResponseDto from(WaybleZoneDocument waybleZoneDocument, Double distance) {
        return WaybleZoneSearchResponseDto.builder()
                .zoneId(waybleZoneDocument.getId())
                .zoneName(waybleZoneDocument.getZoneName())
                .zoneType(waybleZoneDocument.getZoneType())
                .thumbnailImageUrl(waybleZoneDocument.getThumbnailImageUrl())
                .averageRating(waybleZoneDocument.getAverageRating())
                .reviewCount(waybleZoneDocument.getReviewCount())
                .distance(distance)
                .latitude(waybleZoneDocument.getAddress().getLatitude())
                .longitude(waybleZoneDocument.getAddress().getLongitude())
                .build();
    }
}
