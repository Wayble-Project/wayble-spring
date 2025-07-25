package com.wayble.server.explore.dto.search.request;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneDocumentRegisterDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType waybleZoneType,
        String thumbnailImageUrl,
        Address address,
        WaybleZoneFacility facility,
        Double averageRating,
        Long reviewCount,
        Long likes
) {
}
