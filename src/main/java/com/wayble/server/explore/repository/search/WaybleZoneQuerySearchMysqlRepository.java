package com.wayble.server.explore.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.wayble.server.wayblezone.entity.QWaybleZone.waybleZone;

@Repository
@RequiredArgsConstructor
public class WaybleZoneQuerySearchMysqlRepository {

    private final JPAQueryFactory queryFactory;

    private static final int DISTRICT_SEARCH_SIZE = 3;

    /**
     * 조건에 따른 웨이블존 검색 (MySQL 버전)
     */
    public Slice<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(WaybleZoneSearchConditionDto cond, Pageable pageable) {
        
        int fetchSize = pageable.getPageSize() + 1;
        double radius = cond.radiusKm() != null ? cond.radiusKm() : 50.0;

        // Haversine 거리 계산 공식
        NumberExpression<Double> distanceExpression = calculateHaversineDistance(
                cond.latitude(), 
                cond.longitude()
        );

        // 조건 빌더
        BooleanBuilder whereConditions = new BooleanBuilder();

        // 거리 조건 (반경 내)
        whereConditions.and(distanceExpression.loe(radius));

        // 존 타입 조건
        if (cond.zoneType() != null) {
            whereConditions.and(waybleZone.zoneType.eq(cond.zoneType()));
        }

        // 존 이름 조건 (MySQL LIKE 사용)
        if (cond.zoneName() != null && !cond.zoneName().isBlank()) {
            whereConditions.and(waybleZone.zoneName.containsIgnoreCase(cond.zoneName()));
        }

        // 쿼리 실행
        List<WaybleZone> zones = queryFactory
                .selectFrom(waybleZone)
                .leftJoin(waybleZone.facility).fetchJoin()
                .where(whereConditions)
                .orderBy(distanceExpression.asc()) // 거리순 정렬
                .offset((long) pageable.getPageNumber() * fetchSize)
                .limit(fetchSize)
                .fetch();

        // DTO 변환
        List<WaybleZoneSearchResponseDto> dtos = zones.stream()
                .map(zone -> {
                    double distance = calculateHaversineDistanceJava(
                            cond.latitude(), cond.longitude(),
                            zone.getAddress().getLatitude(), zone.getAddress().getLongitude()
                    );
                    return WaybleZoneSearchResponseDto.fromEntity(zone, distance);
                })
                .toList();

        // 다음 페이지 존재 여부 판단
        boolean hasNext = dtos.size() > pageable.getPageSize();
        if (hasNext) {
            dtos = dtos.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(dtos, pageable, hasNext);
    }

    /**
     * 30m 이내이고 이름이 유사한 WaybleZone 찾기 (MySQL 버전)
     */
    public WaybleZoneSearchResponseDto findSimilarWaybleZone(WaybleZoneSearchConditionDto cond) {
        if (cond.zoneName() == null || cond.zoneName().isBlank()) {
            return null;
        }

        // Step 1: 30m(0.03km) 이내 지리적 필터
        NumberExpression<Double> distanceExpression = calculateHaversineDistance(
                cond.latitude(), 
                cond.longitude()
        );

        // Step 2: 거리 필터 + 텍스트 유사도 검색
        List<WaybleZone> candidates = queryFactory
                .selectFrom(waybleZone)
                .leftJoin(waybleZone.facility).fetchJoin()
                .where(distanceExpression.loe(0.03)) // 30m = 0.03km
                .orderBy(distanceExpression.asc())
                .limit(10)
                .fetch();

        // Step 3: 메모리에서 텍스트 유사도 검사
        return candidates.stream()
                .filter(zone -> isTextSimilar(zone.getZoneName(), cond.zoneName()))
                .findFirst()
                .map(zone -> {
                    double distance = calculateHaversineDistanceJava(
                            cond.latitude(), cond.longitude(),
                            zone.getAddress().getLatitude(), zone.getAddress().getLongitude()
                    );
                    return WaybleZoneSearchResponseDto.fromEntity(zone, distance);
                })
                .orElse(null);
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

    /**
     * 텍스트 유사도 검사 (메모리 기반)
     */
    private boolean isTextSimilar(String zoneName, String searchName) {
        if (zoneName == null || searchName == null) {
            return false;
        }
        
        String normalizedZone = normalize(zoneName);
        String normalizedSearch = normalize(searchName);
        
        // 1. 완전 일치
        if (normalizedZone.equals(normalizedSearch)) {
            return true;
        }
        
        // 2. 포함 관계 (기존 wildcard와 유사)
        if (normalizedZone.contains(normalizedSearch) || 
            normalizedSearch.contains(normalizedZone)) {
            return true;
        }
        
        // 3. 편집 거리 (기존 fuzzy와 유사) - 70% 이상 유사
        if (calculateLevenshteinSimilarity(normalizedZone, normalizedSearch) > 0.7) {
            return true;
        }
        
        // 4. 자카드 유사도 (토큰 기반, 기존 match와 유사) - 60% 이상 유사
        return calculateJaccardSimilarity(normalizedZone, normalizedSearch) > 0.6;
    }
    
    /**
     * 텍스트 정규화 (공백, 특수문자 제거)
     */
    private String normalize(String text) {
        return text.replaceAll("\\s+", "")           // 공백 제거
                  .replaceAll("[^가-힣a-zA-Z0-9]", "") // 특수문자 제거
                  .toLowerCase();
    }
    
    /**
     * 레벤슈타인 거리 기반 유사도 (0.0 ~ 1.0)
     */
    private double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - (double) distance / maxLength;
    }
    
    /**
     * 레벤슈타인 거리 계산
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 자카드 유사도 (문자 집합 기반, 0.0 ~ 1.0)
     */
    private double calculateJaccardSimilarity(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }
        
        Set<Character> set1 = s1.chars().mapToObj(c -> (char) c).collect(java.util.stream.Collectors.toSet());
        Set<Character> set2 = s2.chars().mapToObj(c -> (char) c).collect(java.util.stream.Collectors.toSet());
        
        Set<Character> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<Character> union = new java.util.HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
}