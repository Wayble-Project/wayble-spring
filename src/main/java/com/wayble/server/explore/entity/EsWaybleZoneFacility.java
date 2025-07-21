package com.wayble.server.explore.entity;

import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import lombok.*;

@ToString
@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsWaybleZoneFacility {
    
    private boolean hasSlope;
    private boolean hasNoDoorStep;
    private boolean hasElevator;
    private boolean hasTableSeat;
    private boolean hasDisabledToilet;
    private String floorInfo;
    
    public static EsWaybleZoneFacility from(WaybleZoneFacility facility) {
        if (facility == null) {
            return null;
        }
        
        return EsWaybleZoneFacility.builder()
                .hasSlope(facility.isHasSlope())
                .hasNoDoorStep(facility.isHasNoDoorStep())
                .hasElevator(facility.isHasElevator())
                .hasTableSeat(facility.isHasTableSeat())
                .hasDisabledToilet(facility.isHasDisabledToilet())
                .floorInfo(facility.getFloorInfo())
                .build();
    }
}
