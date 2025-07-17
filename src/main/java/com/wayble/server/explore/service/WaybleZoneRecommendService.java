package com.wayble.server.explore.service;

import com.wayble.server.explore.repository.recommend.WaybleZoneQueryRecommendRepository;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.exception.RecommendErrorCase;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleZoneRecommendService {

    private final WaybleZoneQueryRecommendRepository waybleZoneRecommendRepository;

    private final UserRepository userRepository;

    public List<WaybleZoneRecommendResponseDto> getWaybleZonePersonalRecommend(Long userId, double latitude, double longitude, int count) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(RecommendErrorCase.INVALID_USER));

        return waybleZoneRecommendRepository.searchPersonalWaybleZones(user, latitude, longitude, count);
    }
}
