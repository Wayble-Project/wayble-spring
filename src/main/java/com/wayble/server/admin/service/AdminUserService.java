package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserPageDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.user.repository.UserRepository;
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
public class AdminUserService {
    
    private final UserRepository userRepository;
    
    public long getTotalUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            log.error("사용자 수 조회 실패", e);
            return 0;
        }
    }
    
    public AdminUserPageDto getUsersWithPaging(int page, int size) {
        int offset = page * size;
        
        List<AdminUserThumbnailDto> content = userRepository.findUsersWithPaging(offset, size);
        long totalElements = userRepository.count();
        
        log.debug("사용자 페이징 조회 - 페이지: {}, 크기: {}, 전체: {}", page, size, totalElements);
        
        return AdminUserPageDto.of(content, page, size, totalElements);
    }
    
    public List<AdminUserThumbnailDto> findUsersByPage(int page, int size) {
        int offset = page * size;
        return userRepository.findUsersWithPaging(offset, size);
    }
    
    public Optional<AdminUserDetailDto> findUserById(Long userId) {
        return userRepository.findAdminUserDetailById(userId);
    }
}
