package com.wayble.server.explore.repository.recommend;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQueryRecommendRepository {

    private final ElasticsearchOperations operations;

    private static final IndexCoordinates ZONE_INDEX = IndexCoordinates.of("wayble_zone");
    private static final IndexCoordinates LOG_INDEX = IndexCoordinates.of("wayble_zone_visit_log");

    // === [가중치 설정] === //
    private static final double DISTANCE_WEIGHT = 0.6;
    private static final double RECENCY_WEIGHT = 0.2;
    private static final double SIMILARITY_WEIGHT = 0.2;

    public WaybleZoneRecommendResponseDto searchPersonalWaybleZone(User user, double latitude, double longitude) {

        AgeGroup userAgeGroup = AgeGroup.fromBirthDate(user.getBirthDate());
        Gender userGender = user.getGender();

        // 1. 모든 zone을 거리순으로 가져오기 (최대 100개)
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

        // 2. 유사 사용자 방문 기록 가져오기
        NativeQuery logQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q
                        .bool(b -> b
                                .must(m1 -> m1.term(t -> t.field("gender").value(userGender.name())))
                                .must(m2 -> m2.term(t -> t.field("ageGroup").value(userAgeGroup.name())))
                        )
                ))
                .withMaxResults(10000)
                .build();

        SearchHits<WaybleZoneVisitLogDocument> logHits = operations.search(logQuery, WaybleZoneVisitLogDocument.class, LOG_INDEX);

        // 3. 유사 사용자 zone 방문 카운트 계산
        Map<Long, Long> zoneVisitCountMap = logHits.stream()
                .collect(Collectors.groupingBy(hit -> hit.getContent().getZoneId(), Collectors.counting()));

        // 4. 점수 계산
        WaybleZoneDocument best = zoneHits.stream()
                .map(hit -> {
                    WaybleZoneDocument zone = hit.getContent();
                    double distanceScore = 1.0 / (1.0 + ((Double) hit.getSortValues().get(0) / 1000.0)); // km 기준
                    double similarityScore = zoneVisitCountMap.getOrDefault(zone.getZoneId(), 0L) / 10.0;
                    double recencyScore = 1.0; // TODO: 방문 시간 정보 있으면 최근일수록 점수 ↓
                    double totalScore = distanceScore * DISTANCE_WEIGHT + recencyScore * RECENCY_WEIGHT + similarityScore * SIMILARITY_WEIGHT;
                    return Map.entry(zone, totalScore);
                })
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (best == null) return null;
        return WaybleZoneRecommendResponseDto.from(best);
    }
}