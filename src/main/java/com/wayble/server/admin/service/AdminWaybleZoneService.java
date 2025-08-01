package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZonePageDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.admin.repository.AdminWaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminWaybleZoneService {
    private final AdminWaybleZoneRepository adminWaybleZoneRepository;

    public long getTotalWaybleZoneCounts() {
        return adminWaybleZoneRepository.count();
    }

    public AdminWaybleZonePageDto getWaybleZonesWithPaging(int page, int size) {
        // 페이징 데이터 조회
        List<AdminWaybleZoneThumbnailDto> content = adminWaybleZoneRepository.findWaybleZonesWithPaging(page, size);
        
        // 전체 개수 조회
        long totalElements = adminWaybleZoneRepository.count();
        
        log.debug("웨이블존 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        // 페이징 응답 DTO 생성
        return AdminWaybleZonePageDto.of(content, page, size, totalElements);
    }

    public List<AdminWaybleZoneThumbnailDto> findWaybleZonesByPage(int page, int size) {
        return adminWaybleZoneRepository.findWaybleZonesWithPaging(page, size);
    }

    public Optional<AdminWaybleZoneDetailDto> findWaybleZoneById(Long waybleZoneId) {
        return adminWaybleZoneRepository.findAdminWaybleZoneDetailById(waybleZoneId);
    }
}
