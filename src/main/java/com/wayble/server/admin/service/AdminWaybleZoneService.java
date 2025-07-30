package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.AdminWaybleZonePageDto;
import com.wayble.server.admin.dto.AdminWaybleZoneThumbnailDto;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
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
    private final WaybleZoneRepository waybleZoneRepository;

    public long getTotalWaybleZoneCounts() {
        return waybleZoneRepository.count();
    }

    public AdminWaybleZonePageDto getWaybleZonesWithPaging(int page, int size) {
        // 페이징 데이터 조회
        List<AdminWaybleZoneThumbnailDto> content = waybleZoneRepository.findWaybleZonesWithPaging(page, size);
        
        // 전체 개수 조회
        long totalElements = waybleZoneRepository.count();
        
        log.debug("웨이블존 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        // 페이징 응답 DTO 생성
        return AdminWaybleZonePageDto.of(content, page, size, totalElements);
    }

    public List<AdminWaybleZoneThumbnailDto> findWaybleZonesByPage(int page, int size) {
        return waybleZoneRepository.findWaybleZonesWithPaging(page, size);
    }

    public Optional<AdminWaybleZoneDetailDto> findWaybleZoneById(Long waybleZoneId) {
        return waybleZoneRepository.findAdminWaybleZoneDetailById(waybleZoneId);
    }
}
