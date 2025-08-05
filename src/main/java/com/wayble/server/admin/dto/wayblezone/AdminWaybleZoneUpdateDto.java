package com.wayble.server.admin.dto.wayblezone;

import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminWaybleZoneUpdateDto(
        @NotNull(message = "웨이블존 ID는 필수입니다")
        Long id,
        
        @NotBlank(message = "웨이블존 이름은 필수입니다")
        String zoneName,
        
        String contactNumber,
        
        @NotNull(message = "웨이블존 타입은 필수입니다")
        WaybleZoneType zoneType,
        
        @NotBlank(message = "시/도는 필수입니다")
        String state,
        
        @NotBlank(message = "시/군/구는 필수입니다")
        String city,
        
        String district,
        String streetAddress,
        String detailAddress,
        
        @NotNull(message = "위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
        Double latitude,
        
        @NotNull(message = "경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
        Double longitude,
        
        String mainImageUrl
) {
    
    /**
     * AdminWaybleZoneDetailDto로부터 수정용 DTO를 생성
     */
    public static AdminWaybleZoneUpdateDto fromDetailDto(AdminWaybleZoneDetailDto detailDto) {
        return new AdminWaybleZoneUpdateDto(
            detailDto.zoneId(),
            detailDto.zoneName(),
            detailDto.contactNumber(),
            detailDto.zoneType(),
            detailDto.address().getState(),
            detailDto.address().getCity(),
            detailDto.address().getDistrict(),
            detailDto.address().getStreetAddress(),
            detailDto.address().getDetailAddress(),
            detailDto.latitude(),
            detailDto.longitude(),
            detailDto.mainImageUrl()
        );
    }
}