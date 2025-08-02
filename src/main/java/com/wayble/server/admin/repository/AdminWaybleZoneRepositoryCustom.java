package com.wayble.server.admin.repository;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;

import java.util.List;
import java.util.Optional;

public interface AdminWaybleZoneRepositoryCustom {
    
    // 페이징 조회 메서드 (관리자용)
    List<AdminWaybleZoneThumbnailDto> findWaybleZonesWithPaging(int page, int size);
    
    // 상세 조회 메서드 (관리자용)
    Optional<AdminWaybleZoneDetailDto> findAdminWaybleZoneDetailById(Long zoneId);
}