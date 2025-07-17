package com.wayble.server.explore.service;

import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.RecommendLogDocumentRepository;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleZoneRecommendService {

    private final WaybleZoneQueryRecommendRepository waybleZoneRecommendRepository;

    private final RecommendLogDocumentRepository recommendLogDocumentRepository;

    private final WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    private final UserRepository userRepository;

    public List<WaybleZoneRecommendResponseDto> getWaybleZonePersonalRecommend(Long userId, double latitude, double longitude, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(RecommendErrorCase.INVALID_USER));

        WaybleZoneRecommendResponseDto todayRecommendZone = getTodayRecommendZone(userId);
        if(size == 1 && todayRecommendZone != null) {
            return List.of(todayRecommendZone);
        }

        List<WaybleZoneRecommendResponseDto> recommendResponseDtoList = waybleZoneRecommendRepository.searchPersonalWaybleZones(user, latitude, longitude, size);

        if (size == 1 && !recommendResponseDtoList.isEmpty()) {
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

    public WaybleZoneRecommendResponseDto getTodayRecommendZone(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<RecommendLogDocument> recommendLogDocument = recommendLogDocumentRepository.findByUserIdAndRecommendationDate(userId, today);

        if(recommendLogDocument.isPresent()) {
            Long zoneId = recommendLogDocument.get().getZoneId();
            WaybleZoneDocument waybleZoneDocument = waybleZoneDocumentRepository.findById(zoneId)
                    .orElseThrow(() -> new ApplicationException(RecommendErrorCase.WAYBLE_ZONE_NOT_EXIST));

            return WaybleZoneRecommendResponseDto.from(waybleZoneDocument);
        } else {
            return null;
        }
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
                .recommendCount(1L)
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
