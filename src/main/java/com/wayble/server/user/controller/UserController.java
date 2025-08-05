package com.wayble.server.user.controller;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserInfoRegisterRequestDto;
import com.wayble.server.user.dto.UserInfoUpdateRequestDto;
import com.wayble.server.user.dto.UserInfoResponseDto;
import com.wayble.server.user.dto.NicknameCheckResponse;
import com.wayble.server.user.dto.UserRegisterRequestDto;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.service.UserInfoService;
import com.wayble.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    private final UserInfoService userInfoService;

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

    @PostMapping("/info")
    @Operation(summary = "내 정보 등록", description = "유저의 상세 정보를 최초 1회 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 등록 완료"),
            @ApiResponse(responseCode = "400", description = "이미 등록된 정보가 있습니다."),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public CommonResponse<String> registerUserInfo(
            @RequestBody @Valid UserInfoRegisterRequestDto req
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApplicationException(UserErrorCase.FORBIDDEN);
        }

        userInfoService.registerUserInfo(userId, req);
        return CommonResponse.success("내 정보 등록 완료");
    }

    @PatchMapping("/info")
    @Operation(summary = "내 정보 수정", description = "유저가 자신의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 수정 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public CommonResponse<String> updateUserInfo(
            @RequestBody @Valid UserInfoUpdateRequestDto dto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Long)) {
            throw new ApplicationException(UserErrorCase.FORBIDDEN);
        }
        Long userId = (Long) authentication.getPrincipal();

        userInfoService.updateUserInfo(userId, dto);
        return CommonResponse.success("내 정보 수정 완료");
    }

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회", description = "현재 로그인된 유저의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 정보가 존재하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public CommonResponse<UserInfoResponseDto> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApplicationException(UserErrorCase.FORBIDDEN);
        }
        UserInfoResponseDto info = userInfoService.getUserInfo(userId);
        return CommonResponse.success(info);
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "유저가 등록하려고 하는 닉네임이 이미 사용 중인지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 반환"),
            @ApiResponse(responseCode = "400", description = "파라미터 누락 등 잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복")
    })
    public CommonResponse<NicknameCheckResponse> checkNickname(@RequestParam(value = "nickname", required = false) String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ApplicationException(UserErrorCase.NICKNAME_REQUIRED); // 파라미터 누락
        }
        if (!userInfoService.isNicknameAvailable(nickname.trim())) {
            throw new ApplicationException(UserErrorCase.NICKNAME_DUPLICATED); // 닉네임 중복
        }
        return CommonResponse.success(new NicknameCheckResponse(true, "사용 가능한 닉네임입니다."));
    }
}
