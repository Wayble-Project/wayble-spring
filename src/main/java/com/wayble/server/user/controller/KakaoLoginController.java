package com.wayble.server.user.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.KakaoLoginRequestDto;
import com.wayble.server.user.dto.KakaoLoginResponseDto;
import com.wayble.server.user.service.auth.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/login")
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/kakao")
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
}