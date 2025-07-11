package com.wayble.server.recommend.repository;

import com.wayble.server.recommend.dto.WaybleZoneRecommendResponseDto;
import com.wayble.server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WaybleZoneRecommendRepositoryImpl implements WaybleZoneSearchRepositoryCustom {

    private final ElasticsearchOperations operations;

    @Override
    public WaybleZoneRecommendResponseDto searchPersonalWaybleZone(User user) {

        return null;
    }
}
