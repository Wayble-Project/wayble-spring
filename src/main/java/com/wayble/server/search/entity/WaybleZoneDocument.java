package com.wayble.server.search.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "wayble_zone")
public class WaybleZoneDocument {

    @Id
    private Long id;

    private String zoneName;

    private WaybleZoneType zoneType;

    private String thumbnailImageUrl;

    private Address address;

    private double averageRating;

    private long reviewCount;

    public static WaybleZoneDocument from(WaybleZone waybleZone) {
        return WaybleZoneDocument.builder()
                .id(waybleZone.getId())
                .zoneName(waybleZone.getZoneName())
                .zoneType(waybleZone.getZoneType())
                .thumbnailImageUrl("thumbnail image url")
                .address(waybleZone.getAddress())
                .averageRating(0.0)
                .reviewCount(0L)
                .build();
    }
}
