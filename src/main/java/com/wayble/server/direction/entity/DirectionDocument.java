package com.wayble.server.direction.entity;

import com.wayble.server.direction.dto.request.DirectionDocumentRequest;
import com.wayble.server.explore.entity.EsAddress;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "direction", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings/direction_settings.json")
@Mapping(mappingPath = "/elasticsearch/settings/direction_mappings.json")
public class DirectionDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(
            type = FieldType.Text,
            analyzer = "korean_edge_ngram_analyzer",
            searchAnalyzer = "korean_search_analyzer"
    )
    private String name;

    @Field(type = FieldType.Object)
    private EsAddress esAddress;

    @Builder
    private DirectionDocument(Long id, String name, EsAddress esAddress) {
        this.id = id;
        this.name = name;
        this.esAddress = esAddress;
    }

    public static DirectionDocument from(Place place) {
        return DirectionDocument.builder()
                .id(place.getId())
                .name(place.getName())
                .esAddress(EsAddress.from(place.getAddress()))
                .build();
    }

    public static DirectionDocument fromDto(DirectionDocumentRequest request) {
        return DirectionDocument.builder()
                .id(request.placeId())
                .name(request.name())
                .esAddress(EsAddress.from(request.address()))
                .build();
    }
}
