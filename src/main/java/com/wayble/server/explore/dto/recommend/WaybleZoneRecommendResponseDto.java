package com.wayble.server.explore.dto.recommend;

import com.wayble.server.explore.dto.FacilityResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneRecommendResponseDto(

        Long zoneId,

        String zoneName,

        WaybleZoneType zoneType,

        String thumbnailImageUrl,

        Double latitude,

        Double longitude,

        Double averageRating,

        Long reviewCount,

        FacilityResponseDto facility,

        Double distanceScore,

        Double similarityScore,

        Double recencyScore,

        Double totalScore

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
                .facility(FacilityResponseDto.from(waybleZoneDocument.getFacility()))
                .distanceScore(0.0)
                .similarityScore(0.0)
                .recencyScore(0.0)
                .totalScore(0.0)
                .build();
    }
}
