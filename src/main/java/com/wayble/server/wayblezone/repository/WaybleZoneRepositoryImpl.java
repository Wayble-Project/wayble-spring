package com.wayble.server.wayblezone.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;

import java.util.List;

public class WaybleZoneRepositoryImpl implements WaybleZoneRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public WaybleZoneRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<WaybleZoneDistrictResponseDto> findTop3likesWaybleZonesByDistrict(String district) {
        return null;
    }

    @Override
    public List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district) {
        return null;
    }
}
