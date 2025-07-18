package com.wayble.server.explore.entity;

import org.springframework.data.annotation.Id;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.User;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.UUID;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "wayble_zone_visit_log")
public class WaybleZoneVisitLogDocument {

    @Id
    @Field(name = "id")
    private String logId;

    private Long userId;

    private Long zoneId;

    private Gender gender;

    private AgeGroup ageGroup;

    public static WaybleZoneVisitLogDocument fromEntity(User user, Long zoneId) {
        return WaybleZoneVisitLogDocument.builder()
                .logId(UUID.randomUUID().toString())
                .userId(user.getId())
                .zoneId(zoneId)
                .gender(user.getGender())
                .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                .build();
    }
}
