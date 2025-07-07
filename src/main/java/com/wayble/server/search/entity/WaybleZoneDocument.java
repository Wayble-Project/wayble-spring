package com.wayble.server.search.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.search.dto.WaybleZoneDocumentRegisterDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@ToString
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

    public static WaybleZoneDocument fromEntity(WaybleZone waybleZone) {
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

    public static WaybleZoneDocument fromDto(WaybleZoneDocumentRegisterDto dto) {
        return WaybleZoneDocument.builder()
                .id(dto.zoneId())
                .zoneName(dto.zoneName())
                .zoneType(dto.waybleZoneType())
                .thumbnailImageUrl(dto.thumbnailImageUrl())
                .address(dto.address())
                .averageRating(dto.averageRating() != null ? dto.averageRating() : 0.0)
                .reviewCount(dto.reviewCount() != null ? dto.reviewCount() : 0L)
                .build();
    }
}
