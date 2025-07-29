package com.wayble.server.wayblezone.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.explore.dto.common.FacilityResponseDto;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.entity.EsWaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZone;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.wayble.server.wayblezone.entity.QWaybleZone.waybleZone;
import static com.wayble.server.wayblezone.entity.QWaybleZoneVisitLog.waybleZoneVisitLog;

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
                            .address(zone.getAddress().toFullAddress())
                            .latitude(zone.getAddress().getLatitude())
                            .longitude(zone.getAddress().getLongitude())
                            .averageRating(zone.getRating())
                            .reviewCount(zone.getReviewCount())
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
        // 1. 특정 district의 zone들에 대한 visit log 수를 집계하여 TOP3 조회
        List<VisitCount> visitCounts = queryFactory
                .select(Projections.constructor(VisitCount.class,
                        waybleZone.id,
                        waybleZoneVisitLog.count()
                ))
                .from(waybleZone)
                .innerJoin(waybleZoneVisitLog).on(waybleZoneVisitLog.zoneId.eq(waybleZone.id))
                .where(waybleZone.address.district.eq(district))
                .groupBy(waybleZone.id)
                .orderBy(waybleZoneVisitLog.count().desc())
                .limit(3)
                .fetch();

        // 2. 해당 zone들의 상세 정보를 조회하여 DTO로 변환
        return visitCounts.stream()
                .map(visitCount -> {
                    WaybleZone zone = queryFactory
                            .selectFrom(waybleZone)
                            .leftJoin(waybleZone.facility).fetchJoin()
                            .where(waybleZone.id.eq(visitCount.zoneId))
                            .fetchOne();
                    
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
                            .facility(zone.getFacility() != null ? FacilityResponseDto.from(EsWaybleZoneFacility.from(zone.getFacility())) : null)
                            .build();
                    
                    return WaybleZoneDistrictResponseDto.builder()
                            .waybleZoneInfo(waybleZoneInfo)
                            .visitCount(visitCount.count) // visit log 수
                            .likes((long) zone.getLikes())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WaybleZone> findUnsyncedZones(int limit) {
        return queryFactory
                .selectFrom(waybleZone)
                .where(waybleZone.syncedAt.isNull()
                       .or(waybleZone.lastModifiedAt.gt(waybleZone.syncedAt)))
                .limit(limit)
                .fetch();
    }

    @Override
    public List<WaybleZone> findZonesModifiedAfter(LocalDateTime since, int limit) {
        return queryFactory
                .selectFrom(waybleZone)
                .where(waybleZone.lastModifiedAt.gt(since))
                .limit(limit)
                .fetch();
    }

    // 내부 클래스로 visit count 담을 DTO
    public static class VisitCount {
        public final Long zoneId;
        public final Long count;

        public VisitCount(Long zoneId, Long count) {
            this.zoneId = zoneId;
            this.count = count;
        }
    }
}
