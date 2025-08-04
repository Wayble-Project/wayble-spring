package com.wayble.server.wayblezone.repository;

import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.wayblezone.entity.WaybleZone;

import java.time.LocalDateTime;
import java.util.List;

public interface WaybleZoneRepositoryCustom {

    List<WaybleZoneDistrictResponseDto> findTop3likesWaybleZonesByDistrict(String district);

    List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district);
    
    // ES 동기화 관련 메서드들
    List<WaybleZone> findUnsyncedZones(int limit);
    List<WaybleZone> findZonesModifiedAfter(LocalDateTime since, int limit);
}
