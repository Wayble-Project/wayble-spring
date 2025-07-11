package com.wayble.server.recommend.repository;

import com.wayble.server.recommend.dto.WaybleZoneRecommendResponseDto;
import com.wayble.server.user.entity.User;

public interface WaybleZoneSearchRepositoryCustom {

    WaybleZoneRecommendResponseDto searchPersonalWaybleZone(User user);
}
