package com.wayble.server.wayblezone.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.entity.EsWaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZone;

import java.util.List;
import java.util.stream.Collectors;

import static com.wayble.server.wayblezone.entity.QWaybleZone.waybleZone;

public class WaybleZoneRepositoryImpl implements WaybleZoneRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public WaybleZoneRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<WaybleZoneDistrictResponseDto> findTop3likesWaybleZonesByDistrict(String district) {
        // likes 필드를 직접 사용하여 한 번의 쿼리로 조회
        List<WaybleZone> zones = queryFactory
                .selectFrom(waybleZone)
                .leftJoin(waybleZone.facility).fetchJoin()
                .where(waybleZone.address.district.eq(district))
                .orderBy(waybleZone.likes.desc())
                .limit(3)
                .fetch();

        // WaybleZone 엔티티를 DTO로 변환
        return zones.stream()
                .map(zone -> {
                    WaybleZoneInfoResponseDto waybleZoneInfo = WaybleZoneInfoResponseDto.builder()
                            .zoneId(zone.getId())
                            .zoneName(zone.getZoneName())
                            .zoneType(zone.getZoneType())
                            .thumbnailImageUrl(zone.getMainImageUrl())
                            .latitude(zone.getAddress().getLatitude())
                            .longitude(zone.getAddress().getLongitude())
                            .averageRating(zone.getRating())
                            .reviewCount((long) zone.getReviewCount())
                            .facility(zone.getFacility() != null ? FacilityResponseDto.from(EsWaybleZoneFacility.from(zone.getFacility())) : null)
                            .build();
                    
                    return WaybleZoneDistrictResponseDto.builder()
                            .waybleZoneInfo(waybleZoneInfo)
                            .visitCount(0L)
                            .likes((long) zone.getLikes())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district) {
        return null;
    }
}
