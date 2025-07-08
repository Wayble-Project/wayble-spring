package com.wayble.server.search.dto;

import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WaybleZoneSearchConditionDto(
        @NotNull(message = "위도 입력은 필수입니다.")
        Double latitude,

        @NotNull(message = "경도 입력은 필수입니다.")
        Double longitude,

        Double radiusKm,

        @Size(min = 2, message = "zoneName은 최소 2글자 이상이어야 합니다.")
        String zoneName,

        WaybleZoneType zoneType
) {
}
