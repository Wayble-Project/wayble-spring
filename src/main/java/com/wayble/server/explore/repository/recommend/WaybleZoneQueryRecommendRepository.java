package com.wayble.server.explore.repository.recommend;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.entity.WaybleZoneVisitLogDocument;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.explore.entity.AgeGroup;
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

@Repository
@RequiredArgsConstructor
public class WaybleZoneQueryRecommendRepository {

    private final ElasticsearchOperations operations;

    private static final IndexCoordinates ZONE_INDEX = IndexCoordinates.of("wayble_zone");
    private static final IndexCoordinates LOG_INDEX = IndexCoordinates.of("wayble_zone_visit_log");
    private static final IndexCoordinates RECOMMEND_LOG_INDEX = IndexCoordinates.of("recommend_log");

    // === [가중치 설정] === //
    private static final double DISTANCE_WEIGHT = 0.55;
    private static final double SIMILARITY_WEIGHT = 0.15;
    private static final double RECENCY_WEIGHT = 0.3;

    private static final int MAX_DAY_DIFF = 30;

    public List<WaybleZoneRecommendResponseDto> searchPersonalWaybleZones(User user, double latitude, double longitude, int size) {

        AgeGroup userAgeGroup = AgeGroup.fromBirthDate(user.getBirthDate());
        Gender userGender = user.getGender();

        Query geoQuery = Query.of(q -> q
                .bool(b -> b
                        .filter(f -> f.geoDistance(gd -> gd
                                .field("address.location")
                                .location(loc -> loc.latlon(ll -> ll.lat(latitude).lon(longitude)))
                                .distance("50km")))
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(geoQuery)
                .withSort(s -> s.geoDistance(gds -> gds
                        .field("address.location")
                        .location(GeoLocation.of(gl -> gl.latlon(ll -> ll.lat(latitude).lon(longitude))))
                        .order(SortOrder.Asc)))
                .withMaxResults(100)
                .build();

        SearchHits<WaybleZoneDocument> zoneHits = operations.search(nativeQuery, WaybleZoneDocument.class, ZONE_INDEX);

        NativeQuery logQuery = NativeQuery.builder()
                .withMaxResults(10000)
                .build();

        SearchHits<WaybleZoneVisitLogDocument> logHits = operations.search(logQuery, WaybleZoneVisitLogDocument.class, LOG_INDEX);

        Map<Long, Double> zoneVisitScoreMap = new HashMap<>();

        for (var hit : logHits) {
            WaybleZoneVisitLogDocument log = hit.getContent();

            double weight = 0.0;
            boolean ageMatch = log.getAgeGroup() == userAgeGroup;
            boolean genderMatch = log.getGender() == userGender;

            if (ageMatch && genderMatch) {
                weight = 1.0;
            } else if (ageMatch) {
                weight = 0.7;
            } else if (genderMatch) {
                weight = 0.2;
            }

            zoneVisitScoreMap.merge(log.getZoneId(), weight, Double::sum);
        }

        // 최근 추천 날짜 조회
        NativeQuery recommendLogQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.term(t -> t.field("userId").value(user.getId()))))
                .withMaxResults(1000)
                .build();

        SearchHits<RecommendLogDocument> recommendHits = operations.search(recommendLogQuery, RecommendLogDocument.class, RECOMMEND_LOG_INDEX);
        Map<Long, LocalDate> recentRecommendDateMap = recommendHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toMap(RecommendLogDocument::getZoneId, RecommendLogDocument::getRecommendationDate));


        return zoneHits.stream()
                .map(hit -> {
                    WaybleZoneDocument zone = hit.getContent();
                    double distanceScore = (1.0 / (1.0 + ((Double) hit.getSortValues().get(0) / 1000.0))) * DISTANCE_WEIGHT;
                    double similarityScore = (zoneVisitScoreMap.getOrDefault(zone.getZoneId(), 0.0) / 10.0) * SIMILARITY_WEIGHT;
                    double recencyScore = RECENCY_WEIGHT;
                    LocalDate lastRecommendDate = recentRecommendDateMap.get(zone.getZoneId());

                    if (lastRecommendDate != null) {
                        long daysSince = ChronoUnit.DAYS.between(lastRecommendDate, LocalDate.now());
                        double factor = 1.0 - Math.min(daysSince, MAX_DAY_DIFF) / (double) MAX_DAY_DIFF; // 0~1
                        recencyScore = RECENCY_WEIGHT * (1.0 - factor); // days=0 -> 0점, days=30 -> full 점수
                    }

                    double totalScore = distanceScore + similarityScore + recencyScore;

                    return WaybleZoneRecommendResponseDto.builder()
                            .zoneId(zone.getZoneId())
                            .zoneName(zone.getZoneName())
                            .zoneType(zone.getZoneType())
                            .thumbnailImageUrl(zone.getThumbnailImageUrl())
                            .latitude(zone.getAddress().getLocation().getLat())
                            .longitude(zone.getAddress().getLocation().getLon())
                            .averageRating(zone.getAverageRating())
                            .reviewCount(zone.getReviewCount())
                            .distanceScore(distanceScore)
                            .similarityScore(similarityScore)
                            .recencyScore(recencyScore)
                            .totalScore(totalScore)
                            .build();
                })
                .sorted(Comparator.comparingDouble(WaybleZoneRecommendResponseDto::totalScore).reversed())
                .limit(size)
                .toList();
    }
}