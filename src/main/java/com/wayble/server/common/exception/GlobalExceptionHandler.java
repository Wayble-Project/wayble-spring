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
        // 에러 로그 기록 (상세 정보 포함)
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();
        String userAgent = ((ServletWebRequest) request).getRequest().getHeader("User-Agent");
        
        // 스택트레이스에서 실제 에러 발생 위치 추출
        String errorLocation = getErrorLocation(e);
        
        log.error("ApplicationException 발생 - Method: {}, Path: {}, ErrorCode: {}, Message: {}, Location: {}, UserAgent: {}", 
                  method, path, e.getErrorCase(), e.getMessage(), errorLocation, userAgent, e);
        
        CommonResponse commonResponse = CommonResponse.error(e.getErrorCase());

        HttpStatus status = HttpStatus.valueOf(e.getErrorCase().getHttpStatusCode());
        sendToDiscord(e, request, status);

        return ResponseEntity
                .status(e.getErrorCase().getHttpStatusCode())
                .body(commonResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidException(BindingResult bindingResult,
                                                               MethodArgumentNotValidException ex,
                                                               WebRequest request) {
        String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
        
        // 에러 로그 기록
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String method = ((ServletWebRequest) request).getRequest().getMethod();
        String errorLocation = getErrorLocation(ex);
        
        log.error("Validation Exception 발생 - Method: {}, Path: {}, Message: {}, Location: {}", 
                  method, path, message, errorLocation, ex);
        
        CommonResponse commonResponse = CommonResponse.error(400, message);

        sendToDiscord(ex, request, HttpStatus.BAD_REQUEST);
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

        if (!env.acceptsProfiles(Profiles.of("develop"))) {
            log.info("현재 active 프로파일이 develop가 아니므로 Discord 알림을 보내지 않습니다.");
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