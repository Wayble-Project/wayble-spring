package com.wayble.server.admin.dto.wayblezone;

import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneType;

public record AdminWaybleZoneThumbnailDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType zoneType,
        long reviewCount,
        long likes,
        double rating,
        String address,
        WaybleZoneFacility facilityInfo // 시설 정보를 문자열로 요약
) {
}