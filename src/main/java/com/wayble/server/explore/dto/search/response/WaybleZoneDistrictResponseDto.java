package com.wayble.server.explore.dto.search.response;

import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.dto.common.WaybleZoneBaseResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneDistrictResponseDto(
        WaybleZoneBaseResponseDto waybleZoneInfo,
        Long visitCount
) {
    public static WaybleZoneDistrictResponseDto from(WaybleZoneDocument waybleZoneDocument, Long visitCount) {
        
        return WaybleZoneDistrictResponseDto.builder()
                .waybleZoneInfo(WaybleZoneBaseResponseDto.from(waybleZoneDocument))
                .visitCount(visitCount != null ? visitCount : 0L)
                .build();
    }
}