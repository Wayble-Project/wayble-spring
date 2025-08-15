package com.wayble.server.admin.repository;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneNavigationDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;

import java.util.List;
import java.util.Optional;

public interface AdminWaybleZoneRepositoryCustom {
    
    // 페이징 조회 메서드 (관리자용)
    List<AdminWaybleZoneThumbnailDto> findWaybleZonesWithPaging(int page, int size);
    
    // 상세 조회 메서드 (관리자용)
    Optional<AdminWaybleZoneDetailDto> findAdminWaybleZoneDetailById(Long zoneId);
    
    // 이전/다음 웨이블존 ID 조회 (ID순)
    AdminWaybleZoneNavigationDto getNavigationInfo(Long currentId);
}