package com.wayble.server.search.repository;

import com.wayble.server.search.dto.WaybleZoneSearchConditionDto;
import com.wayble.server.search.dto.WaybleZoneSearchResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface WaybleZoneSearchRepositoryCustom {
    Slice<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(WaybleZoneSearchConditionDto cond, Pageable pageable);
}
