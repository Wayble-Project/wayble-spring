package com.wayble.server.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.dto.DiscordWebhookPayload;
import com.wayble.server.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.webhook-url}")
    private String discordWebhookUrl;

    @Autowired
    private Environment env;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<CommonResponse> handleApplicationException(ApplicationException e, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();
        
        log.warn("Application Exception - Method: {}, Path: {}, ErrorCode: {}, Message: {}",
                  method, path, e.getErrorCase(), e.getMessage());
        
        CommonResponse commonResponse = CommonResponse.error(e.getErrorCase());

        return ResponseEntity
                .status(e.getErrorCase().getHttpStatusCode())
                .body(commonResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        
        // 에러 로그 기록
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();
        String errorLocation = getErrorLocation(ex);
        
        log.warn("Validation Exception - Method: {}, Path: {}, Message: {}, Location: {}", 
                  method, path, message, errorLocation);
        
        CommonResponse commonResponse = CommonResponse.error(400, message);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(commonResponse);
    }


    /**
     * 모든 예상하지 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleGeneralException(Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();
        String errorLocation = getErrorLocation(ex);
        
        log.error("Unexpected Exception 발생 - Method: {}, Path: {}, Exception: {}, Message: {}, Location: {}", 
                  method, path, ex.getClass().getSimpleName(), ex.getMessage(), errorLocation, ex);
        
        CommonResponse commonResponse = CommonResponse.error(500, "서버 내부 오류가 발생했습니다.");
        
        sendToDiscord(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(commonResponse);
    }

    private void sendToDiscord(Exception ex, WebRequest request, HttpStatus status) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String timestamp = Instant.now().toString();

        if (!env.acceptsProfiles(Profiles.of("local"))) {
            log.info("현재 active 프로파일이 develop가 아니므로 Discord 알림을 보내지 않습니다.");
            return;
        }

        // 특정 예외 타입 및 경로에 대한 Discord 알림 제외
        if (shouldSkipDiscordNotification(ex, path)) {
            log.debug("Discord 알림 제외 - Exception: {}, Path: {}", ex.getClass().getSimpleName(), path);
            return;
        }

        // Embed 필드 구성
        DiscordWebhookPayload.Embed embed = new DiscordWebhookPayload.Embed(
                "🚨 서버 에러 발생",
                "```" + ex.getMessage() + "```",
                timestamp,
                List.of(
                        new DiscordWebhookPayload.Embed.Field("URL", path, false),
                        new DiscordWebhookPayload.Embed.Field("Status", status.toString(), true),
                        new DiscordWebhookPayload.Embed.Field("Time", timestamp, true),
                        new DiscordWebhookPayload.Embed.Field("Exception", ex.getClass().getSimpleName(), true)
                )
        );
        DiscordWebhookPayload payload = new DiscordWebhookPayload(null, List.of(embed));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try{
            restTemplate.postForEntity(
                    discordWebhookUrl,
                    new HttpEntity<>(payload, headers),
                    String.class
            );
            log.info("send to Discord webhook: {} complete", payload);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    /**
     * Discord 알림을 보내지 않을 예외인지 판단
     */
    private boolean shouldSkipDiscordNotification(Exception ex, String path) {
        String exceptionName = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        
        // 1. NoResourceFoundException 제외 (static resource 요청)
        if ("NoResourceFoundException".equals(exceptionName)) {
            return true;
        }
        
        // 2. 특정 경로 패턴 제외
        if (isIgnoredPath(path)) {
            return true;
        }
        
        // 3. 봇이나 크롤러 요청으로 인한 에러 제외
        if (isBotOrCrawlerRequest(message)) {
            return true;
        }
        
        // 4. 기타 불필요한 예외들
        if (isIgnoredException(exceptionName, message)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 무시할 경로인지 확인
     */
    private boolean isIgnoredPath(String path) {
        String[] ignoredPaths = {
            "/favicon.ico",
            "/index.html", 
            "/robots.txt",
            "/sitemap.xml",
            "/apple-touch-icon",
            "/.well-known/",
            "/wp-admin/",
            "/admin/",
            "/phpmyadmin/",
            "/xmlrpc.php",
            "/.env",
            "/config.php"
        };
        
        for (String ignoredPath : ignoredPaths) {
            if (path.contains(ignoredPath)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 봇이나 크롤러 요청인지 확인
     */
    private boolean isBotOrCrawlerRequest(String message) {
        if (message == null) return false;
        
        String[] botIndicators = {
            "No static resource",
            "Could not resolve view",
            "favicon",
            "robots.txt"
        };
        
        for (String indicator : botIndicators) {
            if (message.contains(indicator)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 무시할 예외인지 확인
     */
    private boolean isIgnoredException(String exceptionName, String message) {
        // 클라이언트 연결 종료 관련
        if ("ClientAbortException".equals(exceptionName) || 
            "BrokenPipeException".equals(exceptionName)) {
            return true;
        }
        
        // 타임아웃 관련 (너무 빈번한 경우)
        if (message != null && (
            message.contains("Connection timed out") ||
            message.contains("Read timed out") ||
            message.contains("Connection reset")
        )) {
            return true;
        }
        
        return false;
    }

    /**
     * 예외의 스택트레이스에서 실제 에러 발생 위치를 추출
     */
    private String getErrorLocation(Exception ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) {
            return "Unknown location";
        }
        
        // com.wayble.server 패키지 내의 첫 번째 스택트레이스를 찾음
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("com.wayble.server")) {
                String className = element.getClassName();
                String fileName = element.getFileName();
                int lineNumber = element.getLineNumber();
                
                // 클래스명에서 패키지 제거 (간결하게 표시)
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                
                return String.format("%s.%s(%s:%d)", 
                    simpleClassName, 
                    element.getMethodName(), 
                    fileName, 
                    lineNumber);
            }
        }
        
        // wayble 패키지 내 코드가 없으면 첫 번째 스택트레이스 반환
        StackTraceElement first = stackTrace[0];
        String className = first.getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return String.format("%s.%s(%s:%d)", 
            simpleClassName, 
            first.getMethodName(), 
            first.getFileName(), 
            first.getLineNumber());
    }
}