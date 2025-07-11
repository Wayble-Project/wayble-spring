package com.wayble.server.search.entity;

import com.wayble.server.search.dto.WaybleZoneDocumentRegisterDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "wayble_zone", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings/wayble_zone_settings.json")
@Mapping(mappingPath = "/elasticsearch/settings/wayble_zone_mappings.json")
public class WaybleZoneDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text,
            analyzer = "korean_edge_ngram_analyzer",
            searchAnalyzer = "korean_search_analyzer")
    private String zoneName;

    private WaybleZoneType zoneType;

    private String thumbnailImageUrl;

    @Field(type = FieldType.Object)
    private EsAddress address;

    private double averageRating;

    private long reviewCount;

    public static WaybleZoneDocument fromEntity(WaybleZone waybleZone) {
        return WaybleZoneDocument.builder()
                .id(waybleZone.getId())
                .zoneName(waybleZone.getZoneName())
                .zoneType(waybleZone.getZoneType())
                .thumbnailImageUrl("thumbnail image url")   // TODO: 이미지 경로 추가
                .address(EsAddress.from(waybleZone.getAddress()))
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
                .address(EsAddress.from(dto.address()))
                .averageRating(dto.averageRating() != null ? dto.averageRating() : 0.0)
                .reviewCount(dto.reviewCount() != null ? dto.reviewCount() : 0L)
                .build();
    }
}
