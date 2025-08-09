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
            
            // 멀티라인 로그 엔트리를 파싱하기 위해 전체 내용을 처리
            List<String> logEntries = parseMultiLineLogEntries(lines);
            
            // 최신 로그부터 처리
            for (int i = logEntries.size() - 1; i >= 0 && errorLogs.size() < limit; i--) {
                String logEntry = logEntries.get(i);
                ErrorLogDto errorLog = ErrorLogDto.from(logEntry);
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
     * 멀티라인 로그 엔트리를 파싱합니다
     */
    private List<String> parseMultiLineLogEntries(List<String> lines) {
        List<String> logEntries = new ArrayList<>();
        StringBuilder currentEntry = new StringBuilder();
        
        for (String line : lines) {
            // 새로운 로그 엔트리 시작을 감지 ([yyyy-MM-dd HH:mm:ss] 패턴)
            if (line.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\].*")) {
                // 이전 엔트리가 있으면 리스트에 추가
                if (currentEntry.length() > 0) {
                    logEntries.add(currentEntry.toString().trim());
                    currentEntry = new StringBuilder();
                }
                currentEntry.append(line);
            } else {
                // 스택트레이스나 멀티라인 메시지의 연속 라인
                if (currentEntry.length() > 0) {
                    currentEntry.append("\n").append(line);
                }
            }
        }
        
        // 마지막 엔트리 추가
        if (currentEntry.length() > 0) {
            logEntries.add(currentEntry.toString().trim());
        }
        
        return logEntries;
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
            
            List<String> lines = Files.readAllLines(logPath);
            List<String> logEntries = parseMultiLineLogEntries(lines);
            
            List<ErrorLogDto> errorLogs = logEntries.stream()
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