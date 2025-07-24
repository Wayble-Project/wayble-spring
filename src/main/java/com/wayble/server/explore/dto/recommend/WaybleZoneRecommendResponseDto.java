package com.wayble.server.explore.dto.recommend;

import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import lombok.Builder;

@Builder
public record WaybleZoneRecommendResponseDto(

        WaybleZoneInfoResponseDto waybleZoneInfo,

        Double distanceScore,

        Double similarityScore,

        Double recencyScore,

        Double totalScore

) {
    public static WaybleZoneRecommendResponseDto from(WaybleZoneDocument waybleZoneDocument) {
        return WaybleZoneRecommendResponseDto.builder()
                .waybleZoneInfo(WaybleZoneInfoResponseDto.from(waybleZoneDocument))
                .distanceScore(0.0)
                .similarityScore(0.0)
                .recencyScore(0.0)
                .totalScore(0.0)
                .build();
    }
}
