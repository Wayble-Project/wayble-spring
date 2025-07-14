package com.wayble.server.explore.entity;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.User;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "wayble_zone_visit_log")
public class WaybleZoneVisitLogDocument {

    @Id
    private Long logId;

    private Long userId;

    private Long zoneId;

    private Gender gender;

    private AgeGroup ageGroup;

    public static WaybleZoneVisitLogDocument fromEntity(User user, Long zoneId) {
        return WaybleZoneVisitLogDocument.builder()
                .userId(user.getId())
                .zoneId(zoneId)
                .gender(user.getGender())
                .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                .build();
    }
}
