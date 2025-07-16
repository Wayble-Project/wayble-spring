package com.wayble.server.user.controller;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserPlaceRequestDto;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.service.UserPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/places")
@RequiredArgsConstructor
public class UserPlaceController {

    private final UserPlaceService userPlaceService;

    @PostMapping
    @Operation(summary = "유저 장소 저장", description = "유저가 웨이블존을 장소로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 저장 성공"),
            @ApiResponse(responseCode = "400", description = "이미 저장한 장소입니다."),
            @ApiResponse(responseCode = "404", description = "해당 유저 또는 웨이블존이 존재하지 않음")
    })
    public CommonResponse<String> saveUserPlace(
            @PathVariable Long userId,
            @RequestBody @Valid UserPlaceRequestDto request,

            // TODO: 로그인 구현 후 Authorization 헤더 필수로 변경 필요
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        // Path variable과 request body의 userId 일치 여부 확인
        if (!userId.equals(request.userId())) {
            throw new ApplicationException(UserErrorCase.INVALID_USER_ID);
        }

        userPlaceService.saveUserPlace(request);
        return CommonResponse.success("장소가 저장되었습니다.");
    }
}
