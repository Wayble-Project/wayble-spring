package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneCreateDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZonePageDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.admin.repository.AdminWaybleZoneRepository;
import com.wayble.server.explore.service.WaybleZoneDocumentService;
import com.wayble.server.wayblezone.entity.WaybleZone;
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
    private final WaybleZoneDocumentService waybleZoneDocumentService;

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
    
    @Transactional
    public Long createWaybleZone(AdminWaybleZoneCreateDto adminWaybleZoneCreateDto) {
        try {
            WaybleZone waybleZone = WaybleZone.fromAdminDto(adminWaybleZoneCreateDto);
            WaybleZone savedZone = adminWaybleZoneRepository.save(waybleZone);
            
            log.info("새 웨이블존 생성 완료 - ID: {}, 이름: {}", savedZone.getId(), savedZone.getZoneName());
            
            try {
                waybleZoneDocumentService.saveDocumentFromEntity(savedZone);
                log.info("WaybleZoneDocument 동기화 완료 - ID: {}", savedZone.getId());
            } catch (Exception esException) {
                log.warn("WaybleZoneDocument 동기화 실패, 데이터 불일치 발생 가능 - ID: {}, 오류: {}", 
                        savedZone.getId(), esException.getMessage());
            }
            
            return savedZone.getId();
        } catch (Exception e) {
            log.error("웨이블존 생성 실패", e);
            throw new RuntimeException("웨이블존 생성에 실패했습니다", e);
        }
    }
}
