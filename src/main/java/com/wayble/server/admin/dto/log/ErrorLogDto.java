package com.wayble.server.admin.dto.log;

import java.time.LocalDateTime;

public record ErrorLogDto(
        LocalDateTime timestamp,
        String level,
        String logger,
        String message,
        String exception,
        String method,
        String path,
        String stackTrace
) {
    public static ErrorLogDto from(String logEntry) {
        try {
            // 멀티라인 로그 엔트리 파싱
            if (!logEntry.contains("ERROR")) {
                return null;
            }
            
            String[] lines = logEntry.split("\n");
            String firstLine = lines[0];
            
            // 타임스탬프 추출
            int timestampEnd = firstLine.indexOf("]");
            if (timestampEnd == -1) return null;
            
            String timestampStr = firstLine.substring(1, timestampEnd);
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"));
            
            // 레벨 추출  
            int levelStart = firstLine.indexOf("ERROR");
            int levelEnd = levelStart + 5;
            String level = "ERROR";
            
            // 로거명 추출
            int loggerStart = levelEnd + 1;
            int loggerEnd = firstLine.indexOf(" - ");
            if (loggerEnd == -1) return null;
            
            String logger = firstLine.substring(loggerStart, loggerEnd).trim();
            
            // 메시지 추출
            String fullMessage = firstLine.substring(loggerEnd + 3);
            
            // HTTP Method, Path, Location 추출
            String method = "";
            String path = "";
            String location = "";
            if (fullMessage.contains("Method:") && fullMessage.contains("Path:")) {
                String[] parts = fullMessage.split(", ");
                for (String part : parts) {
                    if (part.contains("Method:")) {
                        method = part.substring(part.indexOf("Method:") + 8).trim();
                    } else if (part.contains("Path:")) {
                        path = part.substring(part.indexOf("Path:") + 6).trim();
                    } else if (part.contains("Location:")) {
                        location = part.substring(part.indexOf("Location:") + 10).trim();
                    }
                }
            }
            
            // 스택트레이스 추출 (예외 세부정보와 첫 번째 at 라인 찾기)
            String stackTrace = "";
            String exceptionDetail = "";
            
            if (lines.length > 1) {
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    
                    // 예외 세부 정보 라인 (org.springframework.web.bind.MethodArgumentNotValidException: ...)
                    if (line.contains("Exception:") && exceptionDetail.isEmpty()) {
                        // 예외 타입과 상세 설명 포함 (더 긴 길이 허용)
                        if (line.length() > 1000) {
                            exceptionDetail = line.substring(0, 1000) + "...";
                        } else {
                            exceptionDetail = line;
                        }
                        continue;
                    }
                    
                    // 첫 번째 스택트레이스 라인 찾기
                    if (line.startsWith("at ")) {
                        stackTrace = line.substring(3); // "at " 제거
                        break;
                    }
                }
            }
            
            // 예외 정보는 exceptionDetail이 있으면 그것을, 없으면 전체 메시지
            String exception = !exceptionDetail.isEmpty() ? exceptionDetail : fullMessage;
            
            return new ErrorLogDto(timestamp, level, logger, fullMessage, exception, method, path, 
                    stackTrace.isEmpty() ? location : stackTrace);
            
        } catch (Exception e) {
            // 파싱 실패시 null 반환
            return null;
        }
    }
}