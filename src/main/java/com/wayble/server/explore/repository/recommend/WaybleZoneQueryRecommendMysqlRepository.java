package com.wayble.server.explore.repository.recommend;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.entity.EsWaybleZoneFacility;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.wayble.server.wayblezone.entity.QWaybleZone.waybleZone;
import static com.wayble.server.wayblezone.entity.QWaybleZoneVisitLog.waybleZoneVisitLog;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQueryRecommendMysqlRepository {

    private final JPAQueryFactory queryFactory;

    // === [가중치 설정] === //
    private static final double DISTANCE_WEIGHT = 0.55; // 거리 기반 점수 가중치
    private static final double SIMILARITY_WEIGHT = 0.15;   // 유사 사용자 방문 이력 기반 점수 가중치
    private static final double RECENCY_WEIGHT = 0.3;   // 최근 추천 내역 기반 감점 가중치

    private static final int MAX_DAY_DIFF = 30; // 추천 감점 최대 기준일 (30일 전까지 고려)

    public List<WaybleZoneRecommendResponseDto> searchPersonalWaybleZones(User user, double latitude, double longitude, int size) {

        AgeGroup userAgeGroup = AgeGroup.fromBirthDate(user.getBirthDate());
        Gender userGender = user.getGender();

        // Step 1: 50km 반경 이내 장소들 조회 (MySQL Haversine 공식 사용)
        NumberExpression<Double> distanceExpression = calculateHaversineDistance(latitude, longitude);
        
        List<WaybleZone> nearbyZones = queryFactory
                .selectFrom(waybleZone)
                .leftJoin(waybleZone.facility).fetchJoin()
                .where(distanceExpression.loe(50.0)) // 50km 이내
                .orderBy(distanceExpression.asc())
                .limit(100) // 상위 100개만 가져와서 성능 최적화
                .fetch();

        // Step 2: 최근 30일 이내 방문 로그 조회 (MySQL)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        List<WaybleZoneVisitLog> visitLogs = queryFactory
                .selectFrom(waybleZoneVisitLog)
                .where(waybleZoneVisitLog.visitedAt.goe(thirtyDaysAgo))
                .limit(10000)
                .fetch();

        // Step 3: zoneId 별로 유사 사용자 방문 횟수 가중치 계산
        Map<Long, Double> zoneVisitScoreMap = new HashMap<>();

        for (WaybleZoneVisitLog log : visitLogs) {
            double weight = 0.0;
            boolean ageMatch = log.getAgeGroup() == userAgeGroup;
            boolean genderMatch = log.getGender() == userGender;

            if (ageMatch && genderMatch) {
                weight = 1.0;   // 성별, 연령 둘 다 일치
            } else if (ageMatch) {
                weight = 0.7;   // 연령만 일치
            } else if (genderMatch) {
                weight = 0.2;   // 성별만 일치
            }

            zoneVisitScoreMap.merge(log.getZoneId(), weight, Double::sum);
        }

        // Step 4: 최근 추천 날짜 조회 (MySQL - 실제 추천 로그 테이블이 있다면)
        // 여기서는 간단히 빈 맵으로 처리 (실제로는 추천 로그 테이블에서 조회)
        Map<Long, LocalDate> recentRecommendDateMap = new HashMap<>();
        
        // 실제 구현시에는 아래와 같이 추천 로그 테이블에서 조회
        /*
        List<RecommendLog> recommendLogs = queryFactory
                .selectFrom(recommendLog)
                .where(recommendLog.userId.eq(user.getId()))
                .limit(1000)
                .fetch();
        
        Map<Long, LocalDate> recentRecommendDateMap = recommendLogs.stream()
                .collect(Collectors.toMap(
                    RecommendLog::getZoneId, 
                    RecommendLog::getRecommendationDate,
                    (existing, replacement) -> existing.isAfter(replacement) ? existing : replacement
                ));
        */

        // Step 5: 각 장소마다 점수 계산 후 DTO로 변환
        return nearbyZones.stream()
                .map(zone -> {
                    // 거리 계산 (Java로 정확한 계산)
                    double distanceKm = calculateHaversineDistanceJava(
                            latitude, longitude,
                            zone.getAddress().getLatitude(), zone.getAddress().getLongitude()
                    );

                    // 거리 점수 계산 (가까울수록 높음)
                    double distanceScore = (1.0 / (1.0 + distanceKm)) * DISTANCE_WEIGHT;

                    // 유사도 점수 (비슷한 사용자 방문수 반영)
                    double similarityScore = (zoneVisitScoreMap.getOrDefault(zone.getId(), 0.0) / 10.0) * SIMILARITY_WEIGHT;

                    // 최근 추천일 기반 감점 계산
                    double recencyScore = RECENCY_WEIGHT;
                    LocalDate lastRecommendDate = recentRecommendDateMap.get(zone.getId());

                    if (lastRecommendDate != null) {
                        long daysSince = ChronoUnit.DAYS.between(lastRecommendDate, LocalDate.now());
                        double factor = 1.0 - Math.min(daysSince, MAX_DAY_DIFF) / (double) MAX_DAY_DIFF; // 0~1
                        recencyScore = RECENCY_WEIGHT * (1.0 - factor); // days=0 -> 0점, days=30 -> full 점수
                    }

                    double totalScore = distanceScore + similarityScore + recencyScore;

                    WaybleZoneInfoResponseDto waybleZoneInfo = WaybleZoneInfoResponseDto.builder()
                            .zoneId(zone.getId())
                            .zoneName(zone.getZoneName())
                            .zoneType(zone.getZoneType())
                            .thumbnailImageUrl(zone.getMainImageUrl())
                            .address(zone.getAddress().toFullAddress())
                            .latitude(zone.getAddress().getLatitude())
                            .longitude(zone.getAddress().getLongitude())
                            .averageRating(zone.getRating())
                            .reviewCount(zone.getReviewCount())
                            .facility(zone.getFacility() != null ? 
                                    FacilityResponseDto.from(EsWaybleZoneFacility.from(zone.getFacility())) : null)
                            .build();

                    return WaybleZoneRecommendResponseDto.builder()
                            .waybleZoneInfo(waybleZoneInfo)
                            .distanceScore(distanceScore)
                            .similarityScore(similarityScore)
                            .recencyScore(recencyScore)
                            .totalScore(totalScore)
                            .build();
                })
                .sorted(Comparator.comparingDouble(WaybleZoneRecommendResponseDto::totalScore).reversed())  // 점수 내림차순 정렬
                .limit(size)    // 상위 size 개수만 반환
                .toList();
    }

    /**
     * Haversine 거리 계산 (QueryDSL Expression)
     */
    private NumberExpression<Double> calculateHaversineDistance(double userLat, double userLon) {
        // 지구 반지름 (km)
        final double EARTH_RADIUS = 6371.0;
        
        return Expressions.numberTemplate(Double.class,
                "{0} * 2 * ASIN(SQRT(" +
                        "POWER(SIN(RADIANS({1} - {2}) / 2), 2) + " +
                        "COS(RADIANS({2})) * COS(RADIANS({1})) * " +
                        "POWER(SIN(RADIANS({3} - {4}) / 2), 2)" +
                        "))",
                EARTH_RADIUS,
                waybleZone.address.latitude,
                userLat,
                waybleZone.address.longitude,
                userLon
        );
    }

    /**
     * Haversine 거리 계산 (Java 구현)
     */
    private double calculateHaversineDistanceJava(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}