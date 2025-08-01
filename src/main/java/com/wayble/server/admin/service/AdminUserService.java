package com.wayble.server.admin.service;

import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
}
