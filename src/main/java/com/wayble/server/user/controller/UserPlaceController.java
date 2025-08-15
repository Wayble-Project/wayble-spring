package com.wayble.server.user.controller;


import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserPlaceRemoveRequestDto;
import com.wayble.server.user.dto.UserPlaceRequestDto;
import com.wayble.server.user.dto.UserPlaceSummaryDto;
import com.wayble.server.user.service.UserPlaceService;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/places")
@RequiredArgsConstructor
public class UserPlaceController {

    private final UserPlaceService userPlaceService;

    @PostMapping
    @Operation(summary = "유저 장소 저장", description = "유저가 웨이블존을 장소로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 저장 성공"),
            @ApiResponse(responseCode = "400", description = "이미 저장한 장소입니다."),
            @ApiResponse(responseCode = "404", description = "해당 유저 또는 웨이블존이 존재하지 않음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<String> saveUserPlace(
            @RequestBody @Valid UserPlaceRequestDto request
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userPlaceService.saveUserPlace(userId, request); // userId 파라미터로 넘김
        return CommonResponse.success("장소가 저장되었습니다.");
    }

    @GetMapping
    @Operation(
            summary = "내 장소 요약 또는 특정 장소 내 웨이블존 조회",
            description = "placeId 미지정 시 장소 요약 목록(정렬 지원), placeId 지정 시 해당 장소 내 웨이블존 목록(페이징)을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저/장소를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<?> getPlacesOrZones(
            @Parameter(description = "장소 ID (지정 시 해당 장소 내 웨이블존 목록 조회)")
            @RequestParam(required = false) Long placeId,
            @Parameter(description = "요약 목록 정렬: latest(최신순)/name|title(이름순)", schema = @Schema(defaultValue = "latest"))
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @Parameter(description = "페이지 (장소 내 웨이블존 조회 시 사용)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "사이즈 (장소 내 웨이블존 조회 시 사용)", schema = @Schema(defaultValue = "20"))
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        Long userId = extractUserId();

        if (placeId == null) {
            // 장소 요약 목록
            List<UserPlaceSummaryDto> summaries = userPlaceService.getMyPlaceSummaries(userId, sort);
            return CommonResponse.success(summaries);
        } else {
            // 특정 장소 내 웨이블존 목록
            int zeroBased = (page == null ? 0 : Math.max(0, page - 1));
            Page<WaybleZoneListResponseDto> zones =
                    userPlaceService.getZonesInPlace(userId, placeId, zeroBased, size);
            return CommonResponse.success(zones);
        }
    }

    @DeleteMapping
    @Operation(
            summary = "장소에서 웨이블존 제거",
            description = "RequestBody로 placeId, waybleZoneId를 받아 지정한 장소에서 웨이블존을 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제거 성공"),
            @ApiResponse(responseCode = "404", description = "장소 또는 매핑 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<String> removeZoneFromPlace(@RequestBody @Valid UserPlaceRemoveRequestDto request) {
        Long userId = extractUserId();
        userPlaceService.removeZoneFromPlace(userId, request.placeId(), request.waybleZoneId());
        return CommonResponse.success("제거되었습니다.");
    }


    // SecurityContext에서 userId 추출하는 로직
    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object p = auth.getPrincipal();

        if (p instanceof Long l) { return l; }
        if (p instanceof Integer i) { return i.longValue(); }
        if (p instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                // 숫자 변환 실패 시 출력
                System.err.println("Principal 문자열을 Long으로 변환할 수 없습니다: " + s);
            }
        }

        try {
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            throw new IllegalStateException("인증 정보에서 userId를 추출할 수 없습니다. Principal=" + p, e);
        }
    }
}
