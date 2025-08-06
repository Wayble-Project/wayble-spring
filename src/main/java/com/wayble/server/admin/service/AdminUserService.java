package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserPageDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.admin.exception.AdminErrorCase;
import com.wayble.server.admin.repository.AdminUserRepository;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserService {
    
    private final AdminUserRepository adminUserRepository;
    
    public long getTotalUserCount() {
        try {
            return adminUserRepository.count();
        } catch (Exception e) {
            log.error("사용자 수 조회 실패", e);
            return 0;
        }
    }
    
    public AdminUserPageDto getUsersWithPaging(int page, int size) {
        int offset = page * size;
        
        List<Object[]> rawResults = adminUserRepository.findUsersWithPagingRaw(offset, size);
        List<AdminUserThumbnailDto> content = rawResults.stream()
                .map(this::convertToThumbnailDto)
                .toList();
        long totalElements = adminUserRepository.count();
        
        log.debug("사용자 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        return AdminUserPageDto.of(content, page, size, totalElements);
    }
    
    public List<AdminUserThumbnailDto> findUsersByPage(int page, int size) {
        int offset = page * size;
        List<Object[]> rawResults = adminUserRepository.findUsersWithPagingRaw(offset, size);
        return rawResults.stream()
                .map(this::convertToThumbnailDto)
                .toList();
    }
    
    public Optional<AdminUserDetailDto> findUserById(Long userId) {
        List<Object[]> rawResults = adminUserRepository.findAdminUserDetailByIdRaw(userId);
        if (rawResults.isEmpty()) {
            return Optional.empty();
        }
        
        Object[] row = rawResults.get(0);
        long reviewCount = adminUserRepository.countUserReviews(userId);
        long userPlaceCount = adminUserRepository.countUserPlaces(userId);
        
        return Optional.of(convertToDetailDtoWithStats(row, reviewCount, userPlaceCount));
    }
    
    public long getTotalDeletedUserCount() {
        try {
            return adminUserRepository.countDeletedUsers();
        } catch (Exception e) {
            log.error("삭제된 사용자 수 조회 실패", e);
            return 0;
        }
    }
    
    public AdminUserPageDto getDeletedUsersWithPaging(int page, int size) {
        int offset = page * size;
        
        List<Object[]> rawResults = adminUserRepository.findDeletedUsersWithPaging(offset, size);
        List<AdminUserThumbnailDto> content = rawResults.stream()
                .map(this::convertToThumbnailDto)
                .toList();
        long totalElements = adminUserRepository.countDeletedUsers();
        
        log.debug("삭제된 사용자 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        return AdminUserPageDto.of(content, page, size, totalElements);
    }
    
    public List<AdminUserThumbnailDto> findDeletedUsersByPage(int page, int size) {
        int offset = page * size;
        List<Object[]> rawResults = adminUserRepository.findDeletedUsersWithPaging(offset, size);
        return rawResults.stream()
                .map(this::convertToThumbnailDto)
                .toList();
    }
    
    public Optional<AdminUserDetailDto> findDeletedUserById(Long userId) {
        List<Object[]> rawResults = adminUserRepository.findDeletedUserDetailById(userId);
        if (rawResults.isEmpty()) {
            return Optional.empty();
        }
        
        Object[] row = rawResults.get(0);
        log.debug("삭제된 사용자 데이터 조회 - ID: {}, 데이터: {}", userId, java.util.Arrays.toString(row));
        
        // 삭제된 사용자의 경우 삭제된 것들도 포함하여 통계 조회
        long reviewCount = adminUserRepository.countUserReviewsIncludingDeleted(userId);
        long userPlaceCount = adminUserRepository.countUserPlacesIncludingDeleted(userId);
        
        return Optional.of(convertToDetailDto(row, reviewCount, userPlaceCount));
    }
    
    @Transactional
    public void restoreUser(Long userId) {
        try {
            // 삭제된 사용자가 존재하는지 먼저 확인
            Optional<AdminUserDetailDto> deletedUserOpt = findDeletedUserById(userId);
            if (deletedUserOpt.isEmpty()) {
                throw new ApplicationException(AdminErrorCase.USER_NOT_FOUND);
            }
            
            log.info("사용자 복원 시작 - ID: {}, 이메일: {}", userId, deletedUserOpt.get().email());
            
            // 사용자 계정 복원
            int restoredUser = adminUserRepository.restoreUserById(userId);
            log.debug("사용자 계정 복원 완료 - ID: {}, 처리된 행: {}", userId, restoredUser);
            
            // 연관된 리뷰들 복원
            int restoredReviews = adminUserRepository.restoreUserReviews(userId);
            log.debug("사용자 리뷰 복원 완료 - ID: {}, 복원된 리뷰: {}", userId, restoredReviews);
            
            // 리뷰 이미지들 복원
            int restoredReviewImages = adminUserRepository.restoreUserReviewImages(userId);
            log.debug("사용자 리뷰 이미지 복원 완료 - ID: {}, 복원된 이미지: {}", userId, restoredReviewImages);
            
            // 사용자 즐겨찾기 복원
            int restoredUserPlaces = adminUserRepository.restoreUserPlaces(userId);
            log.debug("사용자 즐겨찾기 복원 완료 - ID: {}, 복원된 즐겨찾기: {}", userId, restoredUserPlaces);
            
            log.info("사용자 복원 완료 - ID: {}, 계정: {}, 리뷰: {}, 리뷰이미지: {}, 즐겨찾기: {}", 
                    userId, restoredUser, restoredReviews, restoredReviewImages, restoredUserPlaces);
            
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 복원 실패 - ID: {}", userId, e);
            throw new RuntimeException("사용자 복원에 실패했습니다", e);
        }
    }
    
    private AdminUserThumbnailDto convertToThumbnailDto(Object[] row) {
        Long id = convertToLong(row[0]);
        String nickname = (String) row[1];
        String email = (String) row[2];
        LocalDate birthDate = row[3] != null ? ((Date) row[3]).toLocalDate() : null;
        Gender gender = row[4] != null ? Gender.valueOf((String) row[4]) : null;
        LoginType loginType = row[5] != null ? LoginType.valueOf((String) row[5]) : null;
        UserType userType = row[6] != null ? UserType.valueOf((String) row[6]) : null;
        String disabilityType = (String) row[7];
        String mobilityAid = (String) row[8];
        
        return new AdminUserThumbnailDto(id, nickname, email, birthDate, gender, 
                                       loginType, userType, disabilityType, mobilityAid);
    }
    
    private AdminUserDetailDto convertToDetailDto(Object[] row, long reviewCount, long userPlaceCount) {
        Long id = convertToLong(row[0]);
        String nickname = (String) row[1];
        String username = (String) row[2];
        String email = (String) row[3];
        LocalDate birthDate = row[4] != null ? ((Date) row[4]).toLocalDate() : null;
        Gender gender = row[5] != null ? Gender.valueOf((String) row[5]) : null;
        LoginType loginType = row[6] != null ? LoginType.valueOf((String) row[6]) : null;
        UserType userType = row[7] != null ? UserType.valueOf((String) row[7]) : null;
        String profileImageUrl = (String) row[8];
        String disabilityType = (String) row[9];
        String mobilityAid = (String) row[10];
        Timestamp createdAtStamp = (Timestamp) row[11];
        LocalDateTime createdAt = createdAtStamp != null ? createdAtStamp.toLocalDateTime() : null;
        Timestamp updatedAtStamp = (Timestamp) row[12];
        LocalDateTime updatedAt = updatedAtStamp != null ? updatedAtStamp.toLocalDateTime() : null;
        
        return new AdminUserDetailDto(id, nickname, username, email, birthDate, gender,
                                    loginType, userType, profileImageUrl, disabilityType,
                                    mobilityAid, createdAt, updatedAt, reviewCount, userPlaceCount);
    }
    
    private AdminUserDetailDto convertToDetailDtoWithStats(Object[] row, long reviewCount, long userPlaceCount) {
        Long id = convertToLong(row[0]);
        String nickname = (String) row[1];
        String username = (String) row[2];
        String email = (String) row[3];
        LocalDate birthDate = row[4] != null ? ((Date) row[4]).toLocalDate() : null;
        Gender gender = row[5] != null ? Gender.valueOf((String) row[5]) : null;
        LoginType loginType = row[6] != null ? LoginType.valueOf((String) row[6]) : null;
        UserType userType = row[7] != null ? UserType.valueOf((String) row[7]) : null;
        String profileImageUrl = (String) row[8];
        String disabilityType = (String) row[9];
        String mobilityAid = (String) row[10];
        Timestamp createdAtStamp = (Timestamp) row[11];
        LocalDateTime createdAt = createdAtStamp != null ? createdAtStamp.toLocalDateTime() : null;
        Timestamp updatedAtStamp = (Timestamp) row[12];
        LocalDateTime updatedAt = updatedAtStamp != null ? updatedAtStamp.toLocalDateTime() : null;
        
        return new AdminUserDetailDto(id, nickname, username, email, birthDate, gender,
                                    loginType, userType, profileImageUrl, disabilityType,
                                    mobilityAid, createdAt, updatedAt, reviewCount, userPlaceCount);
    }
    
    private Long convertToLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to Long");
    }
}
