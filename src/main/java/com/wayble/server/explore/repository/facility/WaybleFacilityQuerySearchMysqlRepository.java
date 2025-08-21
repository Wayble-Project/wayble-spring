package com.wayble.server.explore.repository.facility;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.facility.WaybleFacilityConditionDto;
import com.wayble.server.explore.dto.facility.WaybleFacilityResponseDto;
import com.wayble.server.explore.entity.WaybleFacilityMySQL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.wayble.server.explore.entity.QWaybleFacilityMySQL.waybleFacilityMySQL;

@Repository
@RequiredArgsConstructor
public class WaybleFacilityQuerySearchMysqlRepository {

    private final JPAQueryFactory queryFactory;
    
    private static final int LIMIT = 50;
    
    /**
     * 위도, 경도, 시설 타입을 바탕으로 WaybleFacility를 거리순으로 N개 반환 (MySQL/QueryDSL)
     */
    public List<WaybleFacilityResponseDto> findNearbyFacilitiesByType(
            WaybleFacilityConditionDto condition) {
        
        // Haversine 거리 계산식 (QueryDSL Expression)
        NumberExpression<Double> distanceExpression = calculateHaversineDistance(
                condition.latitude(), condition.longitude());
        
        // 조건 빌더
        BooleanBuilder whereClause = new BooleanBuilder();
        
        // 시설 타입 조건 추가
        if (condition.facilityType() != null) {
            whereClause.and(waybleFacilityMySQL.facilityType.eq(condition.facilityType()));
        }
        
        // 반경 10km 이내 필터링
        whereClause.and(distanceExpression.loe(10.0));
        
        List<WaybleFacilityMySQL> facilities = queryFactory
                .selectFrom(waybleFacilityMySQL)
                .where(whereClause)
                .orderBy(distanceExpression.asc())
                .limit(LIMIT)
                .fetch();
        
        return facilities.stream()
                .map(WaybleFacilityResponseDto::fromEntity)
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
                waybleFacilityMySQL.latitude,
                userLat,
                waybleFacilityMySQL.longitude,
                userLon
        );
    }
}