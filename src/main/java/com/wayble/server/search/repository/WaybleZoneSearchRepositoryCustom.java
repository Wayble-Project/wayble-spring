package com.wayble.server.search.repository;

import com.wayble.server.search.dto.WaybleZoneSearchConditionDto;
import com.wayble.server.search.dto.WaybleZoneSearchResponseDto;

import java.util.List;

public interface WaybleZoneSearchRepositoryCustom {
    List<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(WaybleZoneSearchConditionDto cond);
}
