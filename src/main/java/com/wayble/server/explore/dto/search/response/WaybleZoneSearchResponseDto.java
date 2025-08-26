package com.wayble.server.explore.dto.search.response;

import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZone;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record WaybleZoneSearchResponseDto(

        WaybleZoneInfoResponseDto waybleZoneInfo,

        Double distance
) {
    public static WaybleZoneSearchResponseDto from(WaybleZoneDocument waybleZoneDocument, Double distance) {

        return WaybleZoneSearchResponseDto.builder()
                .waybleZoneInfo(WaybleZoneInfoResponseDto.from(waybleZoneDocument))
                .distance(distance)
                .build();
    }
    
    public static WaybleZoneSearchResponseDto fromEntity(WaybleZone waybleZone, Double distance) {

        return WaybleZoneSearchResponseDto.builder()
                .waybleZoneInfo(WaybleZoneInfoResponseDto.fromEntity(waybleZone))
                .distance(distance)
                .build();
    }
}
