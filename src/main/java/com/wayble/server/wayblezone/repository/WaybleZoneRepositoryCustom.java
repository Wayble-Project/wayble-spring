package com.wayble.server.wayblezone.repository;

import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;

import java.util.List;

public interface WaybleZoneRepositoryCustom {

    List<WaybleZoneDistrictResponseDto> findTop3likesWaybleZonesByDistrict(String district);

    List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district);
}
