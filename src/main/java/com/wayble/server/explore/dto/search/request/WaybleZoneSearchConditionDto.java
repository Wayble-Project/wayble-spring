package com.wayble.server.explore.dto.search.request;

import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WaybleZoneSearchConditionDto(
        @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다.")
        @NotNull(message = "위도 입력은 필수입니다.")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다.")
        @NotNull(message = "경도 입력은 필수입니다.")
        Double longitude,

        @DecimalMin(value = "0.1", message = "검색 반경은 100미터 이상이어야 합니다.")
        Double radiusKm,

        @Size(min = 2, message = "zoneName은 최소 2글자 이상이어야 합니다.")
        String zoneName,

        WaybleZoneType zoneType
) {
}
