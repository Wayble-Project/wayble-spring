package com.wayble.server.admin.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneNavigationDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZone;

import java.util.List;
import java.util.Optional;

import static com.wayble.server.wayblezone.entity.QWaybleZone.waybleZone;

public class AdminWaybleZoneRepositoryImpl implements AdminWaybleZoneRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AdminWaybleZoneRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<AdminWaybleZoneThumbnailDto> findWaybleZonesWithPaging(int page, int size) {
        return queryFactory
                .select(Projections.constructor(AdminWaybleZoneThumbnailDto.class,
                        waybleZone.id,
                        waybleZone.zoneName,
                        waybleZone.zoneType,
                        waybleZone.reviewCount,
                        waybleZone.likes,
                        waybleZone.rating,
                        waybleZone.address.state
                                .concat(" ")
                                .concat(waybleZone.address.city.coalesce(""))
                                .concat(" ")
                                .concat(waybleZone.address.district.coalesce(""))
                                .concat(" ")
                                .concat(waybleZone.address.streetAddress.coalesce(""))
                                .concat(" ")
                                .concat(waybleZone.address.detailAddress.coalesce("")),
                        waybleZone.facility
                ))
                .from(waybleZone)
                .leftJoin(waybleZone.facility)
                .orderBy(waybleZone.id.asc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    @Override
    public Optional<AdminWaybleZoneDetailDto> findAdminWaybleZoneDetailById(Long zoneId) {
        // 1. 기본 WaybleZone 정보와 facility 조회
        WaybleZone zone = queryFactory
                .selectFrom(waybleZone)
                .leftJoin(waybleZone.facility).fetchJoin()
                .where(waybleZone.id.eq(zoneId))
                .fetchOne();

        if (zone == null) {
            return Optional.empty();
        }

        // 2. DTO 변환
        AdminWaybleZoneDetailDto.FacilityInfo facilityInfo = null;
        if (zone.getFacility() != null) {
            facilityInfo = new AdminWaybleZoneDetailDto.FacilityInfo(
                    zone.getFacility(),
                    zone.getFacility() + " 이용 가능"
            );
        }

        // 3. 빈 리스트로 초기화 (컬렉션은 나중에 필요시 별도 쿼리로 로드)
        List<AdminWaybleZoneDetailDto.OperatingHourInfo> operatingHours = List.of();
        List<String> imageUrls = List.of();
        List<AdminWaybleZoneDetailDto.RecentReviewInfo> recentReviews = List.of();
        Address address = zone.getAddress();

        // 4. 최종 DTO 생성
        AdminWaybleZoneDetailDto detailDto = new AdminWaybleZoneDetailDto(
                zone.getId(),
                zone.getZoneName(),
                zone.getContactNumber(),
                zone.getZoneType(),
                zone.getAddress(),
                zone.getAddress().toFullAddress(),
                zone.getAddress().getLatitude(),
                zone.getAddress().getLongitude(), 
                zone.getRating(),
                zone.getReviewCount(),
                zone.getLikes(),
                zone.getMainImageUrl(),
                zone.getCreatedAt(),
                zone.getLastModifiedAt(),
                zone.getSyncedAt(),
                facilityInfo,
                operatingHours,
                imageUrls,
                recentReviews
        );

        return Optional.of(detailDto);
    }

    @Override
    public AdminWaybleZoneNavigationDto getNavigationInfo(Long currentId) {
        // 이전 ID 조회 (현재 ID보다 작은 ID 중 가장 큰 값)
        Long previousId = queryFactory
                .select(waybleZone.id.max())
                .from(waybleZone)
                .where(waybleZone.id.lt(currentId))
                .fetchOne();

        // 다음 ID 조회 (현재 ID보다 큰 ID 중 가장 작은 값)
        Long nextId = queryFactory
                .select(waybleZone.id.min())
                .from(waybleZone)
                .where(waybleZone.id.gt(currentId))
                .fetchOne();

        return new AdminWaybleZoneNavigationDto(previousId, nextId);
    }
}