package com.wayble.server.explore.dto.facility;

import com.wayble.server.explore.entity.FacilityType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WaybleFacilityRegisterDto (
        @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다.")
        @NotNull(message = "위도 입력은 필수입니다.")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다.")
        @NotNull(message = "경도 입력은 필수입니다.")
        Double longitude,

        FacilityType facilityType
){
}
