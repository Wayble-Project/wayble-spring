package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneCreateDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZonePageDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneUpdateDto;
import com.wayble.server.admin.exception.AdminErrorCase;
import com.wayble.server.admin.repository.AdminWaybleZoneRepository;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.explore.service.WaybleZoneDocumentService;
import com.wayble.server.user.repository.UserPlaceWaybleZoneMappingRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import com.wayble.server.wayblezone.repository.WaybleZoneVisitLogRepository;
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
    private final WaybleZoneRepository waybleZoneRepository;
    private final UserPlaceWaybleZoneMappingRepository userPlaceWaybleZoneMappingRepository;
    private final WaybleZoneVisitLogRepository waybleZoneVisitLogRepository;

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
    
    @Transactional
    public Long updateWaybleZone(AdminWaybleZoneUpdateDto updateDto) {
        try {
            // 기존 웨이블존 조회
            WaybleZone waybleZone = waybleZoneRepository.findById(updateDto.id())
                    .orElseThrow(() -> new ApplicationException(AdminErrorCase.WAYBLE_ZONE_NOT_FOUND));
            
            // 주소 정보 업데이트
            com.wayble.server.common.entity.Address updatedAddress = com.wayble.server.common.entity.Address
                    .builder()
                    .state(updateDto.state())
                    .city(updateDto.city())
                    .district(updateDto.district())
                    .streetAddress(updateDto.streetAddress())
                    .detailAddress(updateDto.detailAddress())
                    .latitude(updateDto.latitude())
                    .longitude(updateDto.longitude())
                    .build();
            
            // 웨이블존 정보 업데이트
            waybleZone.updateZoneName(updateDto.zoneName());
            waybleZone.updateContactNumber(updateDto.contactNumber());
            waybleZone.updateZoneType(updateDto.zoneType());
            waybleZone.updateAddress(updatedAddress);
            waybleZone.updateMainImageUrl(updateDto.mainImageUrl());
            
            WaybleZone savedZone = waybleZoneRepository.save(waybleZone);
            
            log.info("웨이블존 수정 완료 - ID: {}, 이름: {}", savedZone.getId(), savedZone.getZoneName());
            
            try {
                waybleZoneDocumentService.saveDocumentFromEntity(savedZone);
                log.info("WaybleZoneDocument 동기화 완료 - ID: {}", savedZone.getId());
            } catch (Exception esException) {
                log.warn("WaybleZoneDocument 동기화 실패, 데이터 불일치 발생 가능 - ID: {}, 오류: {}", 
                        savedZone.getId(), esException.getMessage());
            }
            
            return savedZone.getId();
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("웨이블존 수정 실패 - ID: {}", updateDto.id(), e);
            throw new RuntimeException("웨이블존 수정에 실패했습니다", e);
        }
    }

    @Transactional
    public void deleteWaybleZone(Long waybleZoneId) {
        try {
            WaybleZone waybleZone = waybleZoneRepository.findById(waybleZoneId)
                    .orElseThrow(() -> new ApplicationException(AdminErrorCase.WAYBLE_ZONE_NOT_FOUND));

            log.info("웨이블존 삭제 시작 - ID: {}, 이름: {}", waybleZoneId, waybleZone.getZoneName());

            // 연관된 리뷰들 soft delete
            waybleZone.getReviewList().forEach(review -> {
                review.softDelete();
                // 리뷰 이미지들도 함께 soft delete
                review.getReviewImageList().forEach(reviewImage -> reviewImage.softDelete());
            });
            log.debug("웨이블존 리뷰 삭제 완료 - ID: {}, 리뷰 개수: {}", waybleZoneId, waybleZone.getReviewList().size());

            // 운영시간 정보 soft delete
            waybleZone.getOperatingHours().forEach(operatingHour -> {
                operatingHour.softDelete();
            });
            log.debug("웨이블존 운영시간 삭제 완료 - ID: {}, 운영시간 개수: {}", waybleZoneId, waybleZone.getOperatingHours().size());

            // 시설 정보 soft delete (null 체크)
            if (waybleZone.getFacility() != null) {
                waybleZone.getFacility().softDelete();
                log.debug("웨이블존 시설 정보 삭제 완료 - ID: {}", waybleZoneId);
            }

            // 웨이블존 이미지들 soft delete
            waybleZone.getWaybleZoneImageList().forEach(image -> image.softDelete());
            log.debug("웨이블존 이미지 삭제 완료 - ID: {}, 이미지 개수: {}", waybleZoneId, waybleZone.getWaybleZoneImageList().size());

            // 사용자 즐겨찾기 매핑 hard delete (참조 관계 제거)
            userPlaceWaybleZoneMappingRepository.deleteAll(waybleZone.getUserPlaceMappings());
            log.debug("웨이블존 사용자 매핑 삭제 완료 - ID: {}, 매핑 개수: {}", waybleZoneId, waybleZone.getUserPlaceMappings().size());

            // 방문 로그 삭제 (해당 웨이블존의 모든 방문 로그 삭제)
            try {
                waybleZoneVisitLogRepository.deleteByZoneId(waybleZoneId);
                log.debug("웨이블존 방문 로그 삭제 완료 - ID: {}", waybleZoneId);
            } catch (Exception e) {
                log.warn("웨이블존 방문 로그 삭제 실패 - ID: {}, 오류: {}", waybleZoneId, e.getMessage());
            }

            // 웨이블존 자체 soft delete
            waybleZone.softDelete();
            log.info("웨이블존 soft delete 완료 - ID: {}", waybleZoneId);

            // Elasticsearch 문서 삭제
            try {
                waybleZoneDocumentService.deleteDocumentById(waybleZoneId);
                log.info("WaybleZoneDocument 삭제 완료 - ID: {}", waybleZoneId);
            } catch (Exception esException) {
                log.warn("WaybleZoneDocument 삭제 실패, 수동 정리 필요 - ID: {}, 오류: {}", 
                        waybleZoneId, esException.getMessage());
            }

            log.info("웨이블존 삭제 완료 - ID: {}", waybleZoneId);
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("웨이블존 삭제 실패 - ID: {}", waybleZoneId, e);
            throw new RuntimeException("웨이블존 삭제에 실패했습니다", e);
        }
    }
}
