package com.wayble.server.logging.service;

import com.wayble.server.logging.entity.UserActionLog;
import com.wayble.server.logging.repository.UserActionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionLogService {
    
    private final UserActionLogRepository userActionLogRepository;
    
    @Async("loggingTaskExecutor")
    public CompletableFuture<Void> logUserRegister(Long userId, String loginType, String userType) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            UserActionLog userActionLog = UserActionLog.builder()
                    .userId(userId)
                    .action(UserActionLog.ActionType.USER_REGISTER.name())
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .timestamp(LocalDateTime.now())
                    .loginType(loginType)
                    .userType(userType)
                    .build();
                    
            userActionLogRepository.save(userActionLog);
            log.info("User register log saved: userId={}, loginType={}, userType={}", userId, loginType, userType);
        } catch (Exception e) {
            log.error("Failed to save user register log: userId={}", userId, e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    
    public long getTodayUserRegistrationCount() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return userActionLogRepository.countByActionAndTimestampBetween(
                UserActionLog.ActionType.USER_REGISTER.name(), startOfDay, endOfDay);
    }
    
    @Async("loggingTaskExecutor")
    public CompletableFuture<Void> logTokenRefresh(Long userId, String userType) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            // 하루에 한 번만 로그 저장하도록 체크
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            long existingCount = userActionLogRepository.countByUserIdAndActionAndTimestampBetween(
                    userId, UserActionLog.ActionType.USER_TOKEN_REFRESH.name(), startOfDay, endOfDay);
            
            if (existingCount == 0) {
                UserActionLog userActionLog = UserActionLog.builder()
                        .userId(userId)
                        .action(UserActionLog.ActionType.USER_TOKEN_REFRESH.name())
                        .userAgent(request != null ? request.getHeader("User-Agent") : null)
                        .timestamp(LocalDateTime.now())
                        .loginType(null) // 토큰 갱신시에는 loginType 불필요
                        .userType(userType)
                        .build();
                        
                userActionLogRepository.save(userActionLog);
                log.info("User token refresh log saved: userId={}, userType={}", userId, userType);
            } else {
                log.debug("Token refresh already logged today for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to save user token refresh log: userId={}", userId, e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    public long getTodayActiveUserCount() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return userActionLogRepository.countDistinctUserIdByActionAndTimestampBetween(
                UserActionLog.ActionType.USER_TOKEN_REFRESH.name(), startOfDay, endOfDay);
    }
    
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Failed to get current request", e);
            return null;
        }
    }
    
}