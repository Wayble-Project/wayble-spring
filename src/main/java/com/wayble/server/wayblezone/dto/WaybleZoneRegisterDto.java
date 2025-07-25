package com.wayble.server.wayblezone.dto;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneRegisterDto(
        Long zoneId,
        String zoneName,
        String contactNumber,
        WaybleZoneType waybleZoneType,
        String thumbnailImageUrl,
        Address address,
        WaybleZoneFacility facility,
        Double averageRating,
        long reviewCount,
        long likes
) {
}
