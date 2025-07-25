package com.wayble.server.explore.entity;

import com.wayble.server.explore.dto.search.request.WaybleZoneDocumentRegisterDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import org.springframework.data.annotation.Id;
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
    @Field(name = "id")
    private Long zoneId;

    @Field(type = FieldType.Text,
            analyzer = "korean_edge_ngram_analyzer",
            searchAnalyzer = "korean_search_analyzer")
    private String zoneName;

    private WaybleZoneType zoneType;

    private String thumbnailImageUrl;

    @Field(type = FieldType.Object)
    private EsAddress address;

    @Field(type = FieldType.Object)
    private EsWaybleZoneFacility facility;

    private double averageRating;

    private long reviewCount;

    private long likes;

    public static WaybleZoneDocument fromEntity(WaybleZone waybleZone) {
        return WaybleZoneDocument.builder()
                .zoneId(waybleZone.getId())
                .zoneName(waybleZone.getZoneName())
                .zoneType(waybleZone.getZoneType())
                .thumbnailImageUrl(waybleZone.getMainImageUrl() != null ? waybleZone.getMainImageUrl() : null)   // TODO: 이미지 경로 추가
                .address(EsAddress.from(waybleZone.getAddress()))
                .facility(EsWaybleZoneFacility.from(waybleZone.getFacility()))
                .averageRating(0.0)
                .reviewCount(0L)
                .likes(0L)
                .build();
    }

    public static WaybleZoneDocument fromDto(WaybleZoneDocumentRegisterDto dto) {
        return WaybleZoneDocument.builder()
                .zoneId(dto.zoneId())
                .zoneName(dto.zoneName())
                .zoneType(dto.waybleZoneType())
                .thumbnailImageUrl(dto.thumbnailImageUrl())
                .address(EsAddress.from(dto.address()))
                .facility(EsWaybleZoneFacility.from(dto.facility()))
                .averageRating(dto.averageRating() != null ? dto.averageRating() : 0.0)
                .reviewCount(dto.reviewCount() != null ? dto.reviewCount() : 0L)
                .likes(dto.likes() != null ? dto.likes() : 0L)
                .build();
    }
}
