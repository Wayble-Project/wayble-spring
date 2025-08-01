package com.wayble.server.wayblezone.repository;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.wayblezone.entity.WaybleZone;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaybleZoneRepositoryCustom {

    List<WaybleZoneDistrictResponseDto> findTop3likesWaybleZonesByDistrict(String district);

    List<WaybleZoneDistrictResponseDto> findTop3SearchesWaybleZonesByDistrict(String district);
    
    // ES 동기화 관련 메서드들
    List<WaybleZone> findUnsyncedZones(int limit);
    List<WaybleZone> findZonesModifiedAfter(LocalDateTime since, int limit);
    
    // 페이징 조회 메서드 (관리자용)
    List<AdminWaybleZoneThumbnailDto> findWaybleZonesWithPaging(int page, int size);
    
    // 상세 조회 메서드 (관리자용)
    Optional<AdminWaybleZoneDetailDto> findAdminWaybleZoneDetailById(Long zoneId);
}
