package com.wayble.server.explore.repository.recommend;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.wayble.server.wayblezone.entity.QWaybleZoneVisitLog.waybleZoneVisitLog;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQueryRecommendRepository {

    private final ElasticsearchOperations operations;
    private final JPAQueryFactory queryFactory;

    private static final IndexCoordinates ZONE_INDEX = IndexCoordinates.of("wayble_zone");
    private static final IndexCoordinates RECOMMEND_LOG_INDEX = IndexCoordinates.of("recommend_log");


    // === [가중치 설정] === //
    private static final double DISTANCE_WEIGHT = 0.55; // 거리 기반 점수 가중치
    private static final double SIMILARITY_WEIGHT = 0.15;   // 유사 사용자 방문 이력 기반 점수 가중치
    private static final double RECENCY_WEIGHT = 0.3;   // 최근 추천 내역 기반 감점 가중치

    private static final int MAX_DAY_DIFF = 30; // 추천 감점 최대 기준일 (30일 전까지 고려)

    public List<WaybleZoneRecommendResponseDto> searchPersonalWaybleZones(User user, double latitude, double longitude, int size) {

        AgeGroup userAgeGroup = AgeGroup.fromBirthDate(user.getBirthDate());
        Gender userGender = user.getGender();

        // 사용자의 위치 기준으로 50km 반경 이내 장소들 조회
        Query geoQuery = Query.of(q -> q
                .bool(b -> b
                        .filter(f -> f.geoDistance(gd -> gd
                                .field("address.location")
                                .location(loc -> loc.latlon(ll -> ll.lat(latitude).lon(longitude)))
                                .distance("50km")))
                )
        );

        // 거리 기준 정렬하여 최대 100개 결과 조회
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(geoQuery)
                .withSort(s -> s.geoDistance(gds -> gds
                        .field("address.location")
                        .location(GeoLocation.of(gl -> gl.latlon(ll -> ll.lat(latitude).lon(longitude))))
                        .order(SortOrder.Asc)))
                .withMaxResults(100)
                .build();

        SearchHits<WaybleZoneDocument> zoneHits = operations.search(nativeQuery, WaybleZoneDocument.class, ZONE_INDEX);

        // 최근 30일 이내 방문 로그 조회
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        List<WaybleZoneVisitLog> visitLogs = queryFactory
                .selectFrom(waybleZoneVisitLog)
                .where(waybleZoneVisitLog.visitedAt.goe(thirtyDaysAgo))
                .limit(10000)
                .fetch();

        // zoneId 별로 유사 사용자 방문 횟수 가중치 계산
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

        // 최근 추천 날짜 조회 -> 가까울수록 감점
        NativeQuery recommendLogQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.term(t -> t.field("userId").value(user.getId()))))
                .withMaxResults(1000)
                .build();

        SearchHits<RecommendLogDocument> recommendHits = operations.search(recommendLogQuery, RecommendLogDocument.class, RECOMMEND_LOG_INDEX);
        Map<Long, LocalDate> recentRecommendDateMap = recommendHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toMap(RecommendLogDocument::getZoneId, RecommendLogDocument::getRecommendationDate));


        // 각 장소마다 점수 계산 후 DTO로 변환
        return zoneHits.stream()
                .map(hit -> {
                    WaybleZoneDocument zone = hit.getContent();

                    // 거리 점수 계산 (가까울수록 높음)
                    double distanceScore = (1.0 / (1.0 + ((Double) hit.getSortValues().get(0) / 1000.0))) * DISTANCE_WEIGHT;

                    // 유사도 점수 (비슷한 사용자 방문수 반영)
                    double similarityScore = (zoneVisitScoreMap.getOrDefault(zone.getZoneId(), 0.0) / 10.0) * SIMILARITY_WEIGHT;

                    // 최근 추천일 기반 감점 계산
                    double recencyScore = RECENCY_WEIGHT;
                    LocalDate lastRecommendDate = recentRecommendDateMap.get(zone.getZoneId());

                    if (lastRecommendDate != null) {
                        long daysSince = ChronoUnit.DAYS.between(lastRecommendDate, LocalDate.now());
                        double factor = 1.0 - Math.min(daysSince, MAX_DAY_DIFF) / (double) MAX_DAY_DIFF; // 0~1
                        recencyScore = RECENCY_WEIGHT * (1.0 - factor); // days=0 -> 0점, days=30 -> full 점수
                    }

                    double totalScore = distanceScore + similarityScore + recencyScore;

                    WaybleZoneInfoResponseDto waybleZoneInfo = WaybleZoneInfoResponseDto.builder()
                            .zoneId(zone.getZoneId())
                            .zoneName(zone.getZoneName())
                            .zoneType(zone.getZoneType())
                            .thumbnailImageUrl(zone.getThumbnailImageUrl())
                            .latitude(zone.getAddress().getLocation().getLat())
                            .longitude(zone.getAddress().getLocation().getLon())
                            .averageRating(zone.getAverageRating())
                            .reviewCount(zone.getReviewCount())
                            .facility(FacilityResponseDto.from(zone.getFacility()))
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
}