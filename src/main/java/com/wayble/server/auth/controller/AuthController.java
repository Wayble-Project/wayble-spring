package com.wayble.server.auth.controller;

import com.wayble.server.auth.service.AuthService;
import com.wayble.server.auth.service.KakaoLoginService;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.KakaoLoginRequestDto;
import com.wayble.server.user.dto.KakaoLoginResponseDto;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.auth.dto.TokenResponseDto;
import com.wayble.server.user.exception.UserErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/login/basic")
    @Operation(
            summary = "유저 로그인",
            description = "이메일+비밀번호 로그인 및 JWT 발급"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "아이디 혹은 비밀번호가 잘못됨",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class)))
    })
    public CommonResponse<TokenResponseDto> login(@RequestBody @Valid UserLoginRequestDto req) {
        TokenResponseDto tokenDto = authService.login(req);
        return CommonResponse.success(tokenDto);
    }

    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 소셜 로그인", description = "카카오 AccessToken으로 소셜 로그인을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "카카오 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CommonResponse<KakaoLoginResponseDto> kakaoLogin(
            @RequestBody @Valid KakaoLoginRequestDto request
    ) {
        KakaoLoginResponseDto response = kakaoLoginService.kakaoLogin(request);
        return CommonResponse.success(response);
    }

    @PostMapping("/reissue")
    @Operation(
            summary = "AccessToken 재발급",
            description = "클라이언트의 accessToken 만료 시, 유효한 refreshToken으로 새로운 accessToken 및 refreshToken을 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "refreshToken이 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class)))
    })
    public CommonResponse<TokenResponseDto> reissue(
            @Parameter(description = "재발급용 refreshToken", required = true)
            @RequestParam String refreshToken
    ) {
        TokenResponseDto tokens = authService.reissue(refreshToken);
        return CommonResponse.success(tokens);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "유저 로그아웃",
            description = "로그아웃 처리(서버에 저장된 refreshToken을 삭제합니다)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 accessToken")
    })
    public CommonResponse<String> logout(
            @Parameter(
                    description = "사용자 인증용 accessToken (Bearer {token} 형태)",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String accessToken
    ) {
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        String token = accessToken.replace("Bearer ", "");
        if (token.isEmpty()) {
            throw new ApplicationException(UserErrorCase.INVALID_CREDENTIALS);
        }
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.logout(userId);
        return CommonResponse.success("로그아웃에 성공하였습니다.");
    }
}
