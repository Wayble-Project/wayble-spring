package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.log.ErrorLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
public class LogService {
    
    private static final String ERROR_LOG_PATH = "logs/wayble-error.log";
    private static final int MAX_LINES = 1000; // 최대 읽을 라인 수
    
    /**
     * 최근 에러 로그를 조회합니다 (최근 7일간의 로그 파일들을 모두 읽음)
     */
    public List<ErrorLogDto> getRecentErrorLogs(int limit) {
        try {
            List<ErrorLogDto> errorLogs = new ArrayList<>();
            
            // 최근 7일간의 모든 에러 로그 파일을 읽음
            List<Path> logFiles = getErrorLogFiles();
            
            for (Path logFile : logFiles) {
                if (errorLogs.size() >= limit * 2) break; // 충분한 로그를 수집했으면 중단
                
                List<String> lines = readLogFile(logFile);
                List<String> logEntries = parseMultiLineLogEntries(lines);
                
                for (String logEntry : logEntries) {
                    ErrorLogDto errorLog = ErrorLogDto.from(logEntry);
                    if (errorLog != null) {
                        errorLogs.add(errorLog);
                    }
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
     * 최근 7일간의 에러 로그 파일들을 가져옵니다 (최신 순으로 정렬)
     */
    private List<Path> getErrorLogFiles() throws IOException {
        Path logDir = Paths.get("logs");
        if (!Files.exists(logDir)) {
            return List.of();
        }
        
        List<Path> logFiles = new ArrayList<>();
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        
        // 현재 활성 로그 파일
        Path currentLog = Paths.get(ERROR_LOG_PATH);
        if (Files.exists(currentLog)) {
            logFiles.add(currentLog);
        }
        
        // 최근 7일간의 롤링된 로그 파일들만 필터링
        try (Stream<Path> files = Files.list(logDir)) {
            files.filter(path -> {
                String fileName = path.getFileName().toString();
                if (!fileName.startsWith("wayble-error.") || 
                    !(fileName.endsWith(".log") || fileName.endsWith(".log.gz"))) {
                    return false;
                }
                
                // 파일명에서 날짜 추출 (wayble-error.2025-08-07.0.log.gz 형태)
                try {
                    String datePart = fileName.substring("wayble-error.".length(), 
                                                       fileName.indexOf(".", "wayble-error.".length()));
                    LocalDate fileDate = LocalDate.parse(datePart);
                    return !fileDate.isBefore(sevenDaysAgo);
                } catch (Exception e) {
                    return false; // 날짜 파싱 실패시 제외
                }
            })
            .sorted(Comparator.comparing(Path::getFileName).reversed()) // 최신 파일 먼저
            .forEach(logFiles::add);
        }
        
        return logFiles;
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
     * 로그 파일을 읽습니다 (압축 파일과 일반 파일 모두 지원)
     */
    private List<String> readLogFile(Path logFile) throws IOException {
        List<String> lines = new ArrayList<>();
        String fileName = logFile.getFileName().toString();
        
        if (fileName.endsWith(".gz")) {
            // 압축된 파일 읽기
            try (GZIPInputStream gzipStream = new GZIPInputStream(Files.newInputStream(logFile));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzipStream))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } else {
            // 일반 파일 읽기
            lines = Files.readAllLines(logFile);
        }
        
        return lines;
    }
    
    /**
     * 에러 로그 통계를 조회합니다 (최근 7일간의 모든 로그 파일 기준)
     */
    public ErrorLogStats getErrorLogStats() {
        try {
            List<ErrorLogDto> allErrorLogs = new ArrayList<>();
            
            // 최근 7일간의 모든 에러 로그 파일을 읽어서 통계 계산
            List<Path> logFiles = getErrorLogFiles();
            
            for (Path logFile : logFiles) {
                List<String> lines = readLogFile(logFile);
                List<String> logEntries = parseMultiLineLogEntries(lines);
                
                List<ErrorLogDto> errorLogs = logEntries.stream()
                        .map(ErrorLogDto::from)
                        .filter(log -> log != null)
                        .toList();
                
                allErrorLogs.addAll(errorLogs);
            }
            
            long totalErrors = allErrorLogs.size();
            long todayErrors = allErrorLogs.stream()
                    .filter(log -> log.timestamp().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                    .count();
            long lastHourErrors = allErrorLogs.stream()
                    .filter(log -> log.timestamp().isAfter(LocalDateTime.now().minusHours(1)))
                    .count();
                    
            LocalDateTime lastErrorTime = allErrorLogs.stream()
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