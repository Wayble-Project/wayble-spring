package com.wayble.server.explore.dto.recommend;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record WaybleZoneRecommendResponseDto(

        Long zoneId,

        String zoneName,

        WaybleZoneType zoneType,

        String thumbnailImageUrl,

        Double latitude,

        Double longitude,

        Double averageRating,

        Long reviewCount

) {
    public static WaybleZoneRecommendResponseDto from(WaybleZoneDocument waybleZoneDocument) {
        return WaybleZoneRecommendResponseDto.builder()
                .zoneId(waybleZoneDocument.getZoneId())
                .zoneName(waybleZoneDocument.getZoneName())
                .zoneType(waybleZoneDocument.getZoneType())
                .thumbnailImageUrl(waybleZoneDocument.getThumbnailImageUrl())
                .averageRating(waybleZoneDocument.getAverageRating())
                .reviewCount(waybleZoneDocument.getReviewCount())
                .latitude(waybleZoneDocument.getAddress().getLocation().getLat())
                .longitude(waybleZoneDocument.getAddress().getLocation().getLon())
                .build();
    }
}
