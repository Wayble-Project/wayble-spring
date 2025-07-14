package com.wayble.server.explore.dto.search;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.Builder;

@Builder
public record WaybleZoneDocumentRegisterDto(
        Long zoneId,
        String zoneName,
        WaybleZoneType waybleZoneType,
        String thumbnailImageUrl,
        Address address,
        Double averageRating,
        Long reviewCount
) {
}
