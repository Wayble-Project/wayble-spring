package com.wayble.server.explore.dto.search.response;

import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import lombok.Builder;

@Builder
public record WaybleZoneDistrictResponseDto(
        WaybleZoneInfoResponseDto waybleZoneInfo,
        Long visitCount,
        Long likes
) {
    public static WaybleZoneDistrictResponseDto from(WaybleZoneDocument waybleZoneDocument, Long visitCount) {
        
        return WaybleZoneDistrictResponseDto.builder()
                .waybleZoneInfo(WaybleZoneInfoResponseDto.from(waybleZoneDocument))
                .visitCount(visitCount != null ? visitCount : 0L)
                .build();
    }
}