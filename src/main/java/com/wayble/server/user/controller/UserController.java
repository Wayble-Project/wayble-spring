package com.wayble.server.user.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserLoginRequestDto;
import com.wayble.server.user.dto.UserRegisterRequestDto;
import com.wayble.server.user.dto.token.TokenResponseDto;
import com.wayble.server.user.service.UserService;
import com.wayble.server.user.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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

}
