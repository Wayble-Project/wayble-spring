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

    private Address address;

    public static WaybleZoneDocument from(WaybleZone waybleZone) {
        return WaybleZoneDocument.builder()
                .id(waybleZone.getId())
                .zoneName(waybleZone.getZoneName())
                .zoneType(waybleZone.getZoneType())
                .address(waybleZone.getAddress())
                .build();
    }
}
