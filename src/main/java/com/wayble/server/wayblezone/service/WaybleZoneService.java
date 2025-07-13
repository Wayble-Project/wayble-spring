package com.wayble.server.wayblezone.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneImage;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import com.wayble.server.wayblezone.exception.WaybleZoneErrorCase;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto.*;

@Service
@RequiredArgsConstructor
public class WaybleZoneService {

    private final WaybleZoneRepository waybleZoneRepository;

    public List<WaybleZoneListResponseDto> getWaybleZones(String city, String category) {
        WaybleZoneType zoneType;

        try {
            zoneType = WaybleZoneType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(WaybleZoneErrorCase.INVALID_CATEGORY);
        }

        List<WaybleZone> zones = waybleZoneRepository.findByAddress_CityContainingAndZoneType(city, zoneType);

        return zones.stream().map(zone -> {
            WaybleZoneFacility f = zone.getFacility();
            if (f == null) {
                throw new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_FACILITY_NOT_FOUND);
            }

            WaybleZoneImage image = zone.getWaybleZoneImageList().stream().findFirst().orElse(null);

            return WaybleZoneListResponseDto.builder()
                    .waybleZoneId(zone.getId())
                    .name(zone.getZoneName())
                    .category(zone.getZoneType().toString())
                    .address(zone.getAddress().toFullAddress())
                    .rating(zone.getRating())
                    .reviewCount(zone.getReviewCount())
                    .imageUrl(image != null ? image.getImageUrl() : null)
                    .contactNumber(zone.getContactNumber())
                    .facilities(FacilityDto.builder()
                            .hasSlope(f.isHasSlope())
                            .hasNoDoorStep(f.isHasNoDoorStep())
                            .hasElevator(f.isHasElevator())
                            .hasTableSeat(f.isHasTableSeat())
                            .hasDisabledToilet(f.isHasDisabledToilet())
                            .floorInfo(f.getFloorInfo())
                            .build()
                    ).build();
        }).toList();
    }
}