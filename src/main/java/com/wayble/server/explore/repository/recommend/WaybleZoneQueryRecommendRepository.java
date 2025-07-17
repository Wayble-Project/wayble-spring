package com.wayble.server.explore.repository.recommend;

import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQueryRecommendRepository {

    private final ElasticsearchOperations operations;

    public WaybleZoneRecommendResponseDto searchPersonalWaybleZone(User user) {
        return null;
    }
}
