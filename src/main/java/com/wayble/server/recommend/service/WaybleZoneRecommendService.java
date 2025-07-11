package com.wayble.server.recommend.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.recommend.dto.WaybleZoneRecommendResponseDto;
import com.wayble.server.recommend.exception.RecommendErrorCase;
import com.wayble.server.recommend.repository.WaybleZoneRecommendRepository;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaybleZoneRecommendService {

    private final WaybleZoneRecommendRepository waybleZoneRecommendRepository;

    private final UserRepository userRepository;

    public WaybleZoneRecommendResponseDto getWaybleZonePersonalRecommend(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(RecommendErrorCase.INVALID_USER));

        return waybleZoneRecommendRepository.searchPersonalWaybleZone(user);
    }
}
