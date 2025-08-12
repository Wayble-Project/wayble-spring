package com.wayble.server.explore.entity;

import com.wayble.server.direction.entity.transportation.Facility;
import com.wayble.server.explore.dto.facility.WaybleFacilityRegisterDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "wayble_facility_document", createIndex = true)
public class WaybleFacilityDocument {
    @Id
    @Field(name = "id")
    private String id;

    @GeoPointField
    private GeoPoint location;

    private FacilityType facilityType;
}
