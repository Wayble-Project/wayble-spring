package com.wayble.server.user.controller;


import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.UserPlaceRemoveRequestDto;
import com.wayble.server.user.dto.UserPlaceRequestDto;
import com.wayble.server.user.dto.UserPlaceSummaryDto;
import com.wayble.server.user.service.UserPlaceService;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "내 장소 리스트 요약 조회", description = "장소 관련 목록(리스트)만 반환합니다(개수 포함).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<List<UserPlaceSummaryDto>> getMyPlaceSummaries(
            @RequestParam(name = "sort", defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId();
        List<UserPlaceSummaryDto> summaries = userPlaceService.getMyPlaceSummaries(userId, sort);
        return CommonResponse.success(summaries);
    }


    @GetMapping("/zones")
    @Operation(summary = "특정 장소 내 웨이블존 목록 조회(페이징)",
            description = "placeId로 해당 장소 내부의 웨이블존 카드 목록을 반환합니다. (page는 1부터 시작.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저/장소를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<Page<WaybleZoneListResponseDto>> getZonesInPlace(
            @RequestParam Long placeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        Long userId = extractUserId();
        Page<WaybleZoneListResponseDto> zones = userPlaceService.getZonesInPlace(userId, placeId, page, size);
        return CommonResponse.success(zones);
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
