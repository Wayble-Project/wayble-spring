package com.wayble.server.explore.entity;
import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate recommendationDate;

    private Long recommendCount;

    public void updateRecommendLog(LocalDate recommendationDate, Long recommendCount) {
        if(recommendationDate != null) {
            this.recommendationDate = recommendationDate;
        }
        if(recommendCount != null) {
            this.recommendCount = recommendCount;
        }
    }
}
