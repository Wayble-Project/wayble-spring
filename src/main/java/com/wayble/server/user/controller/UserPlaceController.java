package com.wayble.server.user.controller;


import com.wayble.server.auth.resolver.CurrentUser;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.user.dto.*;
import com.wayble.server.user.service.UserPlaceService;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/users/places")
@RequiredArgsConstructor
public class UserPlaceController {

    private final UserPlaceService userPlaceService;

    @PostMapping
    @Operation(summary = "웨이블존 저장할 리스트 생성",
            description = "제목과 색상을 받아 웨이블존을 저장할 리스트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리스트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "동일한 리스트명이 이미 존재")
    })
    public CommonResponse<UserPlaceCreateResponseDto> createPlaceList(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody @Valid UserPlaceCreateRequestDto request
    ) {
        Long placeId = userPlaceService.createPlaceList(userId, request);
        String normalizedTitle = request.title().trim();
        String normalizedColor = (request.color() == null || request.color().isBlank())
                ? "GRAY"
                : request.color().trim().toUpperCase();
        return CommonResponse.success(
                UserPlaceCreateResponseDto.builder()
                        .placeId(placeId)
                        .title(normalizedTitle)
                        .color(normalizedColor)
                        .message("리스트가 생성되었습니다.")
                        .build()
        );
    }

    @GetMapping
    @Operation(summary = "내가 저장한 리스트 요약 조회", description = "장소 관련 목록(리스트)만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<List<UserPlaceSummaryDto>> getMyPlaceSummaries(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(name = "sort", defaultValue = "latest") String sort
    ) {
        List<UserPlaceSummaryDto> summaries = userPlaceService.getMyPlaceSummaries(userId, sort);
        return CommonResponse.success(summaries);
    }

    @DeleteMapping
    @Operation(
            summary = "내가 저장한 리스트에서 웨이블존 제거",
            description = "RequestBody로 placeId, waybleZoneId를 받아 지정한 장소에서 웨이블존을 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제거 성공"),
            @ApiResponse(responseCode = "404", description = "장소 또는 매핑 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<String> removeZoneFromPlace(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody @Valid UserPlaceRemoveRequestDto request
    ) {
        userPlaceService.removeZoneFromPlace(userId, request.placeId(), request.waybleZoneId());
        return CommonResponse.success("성공적으로 제거되었습니다.");
    }

    @PostMapping("/zones")
    @Operation(summary = "리스트에 웨이블존 추가",
            description = "placeId와 waybleZoneId 배열을 받아 여러 웨이블존을 리스트에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "웨이블존 추가 성공"),
            @ApiResponse(responseCode = "404", description = "유저/리스트/웨이블존을 찾을 수 없음")
    })
    public CommonResponse<String> addZonesToPlace(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestBody @Valid UserPlaceAddZonesRequestDto request
    ) {
        userPlaceService.addZonesToPlace(userId, request.placeId(), request.waybleZoneIds());
        return CommonResponse.success("리스트에 웨이블존이 추가되었습니다.");
    }

    @GetMapping("/zones")
    @Operation(summary = "저장한 리스트 내 웨이블존 목록 조회(페이징)",
            description = "placeId로 해당 장소 내부의 웨이블존 목록을 반환합니다. (page는 0부터 시작.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저/장소를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    public CommonResponse<Page<WaybleZoneListResponseDto>> getZonesInPlace(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam Long placeId,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        Page<WaybleZoneListResponseDto> zones = userPlaceService.getZonesInPlace(userId, placeId, page, size);
        return CommonResponse.success(zones);
    }
}
