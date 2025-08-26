package com.wayble.server.explore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wayble_facility")
public class WaybleFacilityMySQL {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false)
    private FacilityType facilityType;
}