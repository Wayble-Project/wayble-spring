package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserPageDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.admin.repository.AdminUserRepository;
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
        
        List<AdminUserThumbnailDto> content = adminUserRepository.findUsersWithPaging(offset, size);
        long totalElements = adminUserRepository.count();
        
        log.debug("사용자 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        return AdminUserPageDto.of(content, page, size, totalElements);
    }
    
    public List<AdminUserThumbnailDto> findUsersByPage(int page, int size) {
        int offset = page * size;
        return adminUserRepository.findUsersWithPaging(offset, size);
    }
    
    public Optional<AdminUserDetailDto> findUserById(Long userId) {
        return adminUserRepository.findAdminUserDetailById(userId);
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
        return Optional.of(convertToDetailDto(rawResults.get(0)));
    }
    
    private AdminUserThumbnailDto convertToThumbnailDto(Object[] row) {
        Long id = convertToLong(row[0]);
        String nickname = (String) row[1];
        String email = (String) row[2];
        LocalDate birthDate = row[3] != null ? ((Date) row[3]).toLocalDate() : null;
        Gender gender = row[4] != null ? Gender.valueOf((String) row[4]) : null;
        LoginType loginType = LoginType.valueOf((String) row[5]);
        UserType userType = UserType.valueOf((String) row[6]);
        String disabilityType = (String) row[7];
        String mobilityAid = (String) row[8];
        
        return new AdminUserThumbnailDto(id, nickname, email, birthDate, gender, 
                                       loginType, userType, disabilityType, mobilityAid);
    }
    
    private AdminUserDetailDto convertToDetailDto(Object[] row) {
        Long id = convertToLong(row[0]);
        String nickname = (String) row[1];
        String username = (String) row[2];
        String email = (String) row[3];
        LocalDate birthDate = row[4] != null ? ((Date) row[4]).toLocalDate() : null;
        Gender gender = row[5] != null ? Gender.valueOf((String) row[5]) : null;
        LoginType loginType = LoginType.valueOf((String) row[6]);
        UserType userType = UserType.valueOf((String) row[7]);
        String profileImageUrl = (String) row[8];
        String disabilityType = (String) row[9];
        String mobilityAid = (String) row[10];
        Timestamp createdAtStamp = (Timestamp) row[11];
        LocalDateTime createdAt = createdAtStamp.toLocalDateTime();
        Timestamp updatedAtStamp = (Timestamp) row[12];
        LocalDateTime updatedAt = updatedAtStamp.toLocalDateTime();
        
        return new AdminUserDetailDto(id, nickname, username, email, birthDate, gender,
                                    loginType, userType, profileImageUrl, disabilityType,
                                    mobilityAid, createdAt, updatedAt);
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
