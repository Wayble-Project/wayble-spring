package com.wayble.server.direction.entity;

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

    @Field(type = FieldType.Text)
    private String address;

    @Builder
    public DirectionDocument(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public static DirectionDocument of(String name, String address) {
        return DirectionDocument.builder()
                .name(name)
                .address(address)
                .build();
    }
}
