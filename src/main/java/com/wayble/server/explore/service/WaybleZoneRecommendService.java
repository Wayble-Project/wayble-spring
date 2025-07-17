package com.wayble.server.explore.service;

import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.repository.RecommendLogDocumentRepository;
import com.wayble.server.explore.repository.recommend.WaybleZoneQueryRecommendRepository;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.exception.RecommendErrorCase;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleZoneRecommendService {

    private final WaybleZoneQueryRecommendRepository waybleZoneRecommendRepository;

    private final RecommendLogDocumentRepository recommendLogDocumentRepository;

    private final UserRepository userRepository;

    public List<WaybleZoneRecommendResponseDto> getWaybleZonePersonalRecommend(Long userId, double latitude, double longitude, int count) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(RecommendErrorCase.INVALID_USER));

        List<WaybleZoneRecommendResponseDto> recommendResponseDtoList = waybleZoneRecommendRepository.searchPersonalWaybleZones(user, latitude, longitude, count);

        if (count == 1 && !recommendResponseDtoList.isEmpty()) {
            Long zoneId = recommendResponseDtoList.get(0).zoneId();

            boolean logExist = recommendLogDocumentRepository.existsByUserIdAndZoneId(userId, zoneId);
            if (logExist) {
                updateRecommendLog(userId, zoneId);
            } else {
                saveRecommendLog(userId, zoneId);
            }
        }

        return recommendResponseDtoList;
    }

    public void saveRecommendLog(Long userId, Long zoneId) {
        String logId = UUID.randomUUID().toString();
        LocalDate dateNow = LocalDate.now();

        RecommendLogDocument recommendLogDocument = RecommendLogDocument
                .builder()
                .id(logId)
                .userId(userId)
                .zoneId(zoneId)
                .recommendationDate(dateNow)
                .build();

        recommendLogDocumentRepository.save(recommendLogDocument);
    }

    public void updateRecommendLog(Long userId, Long zoneId) {
        RecommendLogDocument recommendLogDocument = recommendLogDocumentRepository.findByUserIdAndZoneId(userId, zoneId)
                .orElseThrow(() -> new ApplicationException(RecommendErrorCase.RECOMMEND_LOG_NOT_EXIST));

        Long recommendCount = recommendLogDocument.getRecommendCount() + 1;
        recommendLogDocument.updateRecommendLog(LocalDate.now(), recommendCount);

        recommendLogDocumentRepository.save(recommendLogDocument);
    }
}
