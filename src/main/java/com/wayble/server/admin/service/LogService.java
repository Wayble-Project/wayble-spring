package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.log.ErrorLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class LogService {
    
    private static final String ERROR_LOG_PATH = "logs/wayble-error.log";
    private static final int MAX_LINES = 1000; // 최대 읽을 라인 수
    
    /**
     * 최근 에러 로그를 조회합니다
     */
    public List<ErrorLogDto> getRecentErrorLogs(int limit) {
        try {
            Path logPath = Paths.get(ERROR_LOG_PATH);
            
            if (!Files.exists(logPath)) {
                log.warn("에러 로그 파일이 존재하지 않습니다: {}", ERROR_LOG_PATH);
                return List.of();
            }
            
            List<String> lines = Files.readAllLines(logPath);
            List<ErrorLogDto> errorLogs = new ArrayList<>();
            
            // 마지막 라인부터 역순으로 처리 (최신 로그가 아래쪽)
            int startIndex = Math.max(0, lines.size() - MAX_LINES);
            for (int i = lines.size() - 1; i >= startIndex && errorLogs.size() < limit; i--) {
                String line = lines.get(i);
                ErrorLogDto errorLog = ErrorLogDto.from(line);
                if (errorLog != null) {
                    errorLogs.add(errorLog);
                }
            }
            
            return errorLogs.stream()
                    .sorted(Comparator.comparing(ErrorLogDto::timestamp).reversed())
                    .limit(limit)
                    .toList();
                    
        } catch (IOException e) {
            log.error("에러 로그 파일 읽기 실패", e);
            return List.of();
        }
    }
    
    /**
     * 에러 로그 통계를 조회합니다
     */
    public ErrorLogStats getErrorLogStats() {
        try {
            Path logPath = Paths.get(ERROR_LOG_PATH);
            
            if (!Files.exists(logPath)) {
                return new ErrorLogStats(0, 0, 0, LocalDateTime.now());
            }
            
            try (Stream<String> lines = Files.lines(logPath)) {
                List<ErrorLogDto> errorLogs = lines
                        .map(ErrorLogDto::from)
                        .filter(log -> log != null)
                        .toList();
                
                long totalErrors = errorLogs.size();
                long todayErrors = errorLogs.stream()
                        .filter(log -> log.timestamp().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                        .count();
                long lastHourErrors = errorLogs.stream()
                        .filter(log -> log.timestamp().isAfter(LocalDateTime.now().minusHours(1)))
                        .count();
                        
                LocalDateTime lastErrorTime = errorLogs.stream()
                        .map(ErrorLogDto::timestamp)
                        .max(Comparator.naturalOrder())
                        .orElse(null);
                        
                return new ErrorLogStats(totalErrors, todayErrors, lastHourErrors, lastErrorTime);
            }
            
        } catch (IOException e) {
            log.error("에러 로그 통계 조회 실패", e);
            return new ErrorLogStats(0, 0, 0, LocalDateTime.now());
        }
    }
    
    /**
     * 에러 로그 통계 정보
     */
    public record ErrorLogStats(
            long totalErrors,
            long todayErrors, 
            long lastHourErrors,
            LocalDateTime lastErrorTime
    ) {}
}