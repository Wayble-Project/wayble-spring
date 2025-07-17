package com.wayble.server.explore.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.LocalDate;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "recommend_log")
public class RecommendLogDocument {

    @Id
    @Field(name = "id")
    private String id;

    private Long userId;

    private Long zoneId;

    private LocalDate recommendationDate;

    private Long recommendCount;

    public void updateRecommendLog(LocalDate recommendationDate, Long recommendCount) {
        this.recommendationDate = recommendationDate;
        this.recommendCount = recommendCount;
    }
}
