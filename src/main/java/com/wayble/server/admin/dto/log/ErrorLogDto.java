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
    public static ErrorLogDto from(String logLine) {
        try {
            // 로그 파싱: [2025-08-07 15:30:45] [main] ERROR com.wayble.server.SomeClass - Error message
            if (!logLine.contains("ERROR")) {
                return null;
            }
            
            // 타임스탬프 추출
            int timestampEnd = logLine.indexOf("]");
            if (timestampEnd == -1) return null;
            
            String timestampStr = logLine.substring(1, timestampEnd);
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"));
            
            // 레벨 추출  
            int levelStart = logLine.indexOf("ERROR");
            int levelEnd = levelStart + 5;
            String level = "ERROR";
            
            // 로거명 추출
            int loggerStart = levelEnd + 1;
            int loggerEnd = logLine.indexOf(" - ");
            if (loggerEnd == -1) return null;
            
            String logger = logLine.substring(loggerStart, loggerEnd).trim();
            
            // 메시지 추출
            String fullMessage = logLine.substring(loggerEnd + 3);
            
            // HTTP Method와 Path 추출
            String method = "";
            String path = "";
            if (fullMessage.contains("Method:") && fullMessage.contains("Path:")) {
                String[] parts = fullMessage.split(", ");
                for (String part : parts) {
                    if (part.contains("Method:")) {
                        method = part.substring(part.indexOf("Method:") + 8).trim();
                    } else if (part.contains("Path:")) {
                        path = part.substring(part.indexOf("Path:") + 6).trim();
                    }
                }
            }
            
            // 예외 정보는 별도 처리 (여러 줄일 수 있음)
            String exception = "";
            if (fullMessage.contains("Exception") || fullMessage.contains("Error")) {
                exception = fullMessage;
            }
            
            return new ErrorLogDto(timestamp, level, logger, fullMessage, exception, method, path, "");
            
        } catch (Exception e) {
            // 파싱 실패시 null 반환
            return null;
        }
    }
}