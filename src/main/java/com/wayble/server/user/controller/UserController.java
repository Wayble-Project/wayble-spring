package com.wayble.server.user.controller;

import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.user.dto.UserRegisterRequestDto;
import com.wayble.server.user.dto.token.TokenResponseDto;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.service.UserService;
import com.wayble.server.user.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtProvider;

    @PostMapping("/signup")
    @Operation(
            summary = "유저 회원가입",
            description = "신규 유저 회원가입을 수행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 회원",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class)))
    })
    public CommonResponse<String> signup(@RequestBody @Valid UserRegisterRequestDto req) {
        userService.signup(req);
        return CommonResponse.success("회원가입 성공");
    }

    @PostMapping("/login")
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

    @PostMapping("/reissue")
    @Operation(
            summary = "AccessToken 재발급",
            description = "클라이언트의 accessToken 만료 시, 유효한 refreshToken으로 새로운 accessToken 및 refreshToken을 재발급합니다."


    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.user.dto.token.TokenResponseDto.class))),
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
        Long userId = jwtProvider.getUserId(token);
        authService.logout(userId);
        return CommonResponse.success("로그아웃에 성공하였습니다.");
    }

}
