package com.wayble.server.wayblezone.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.wayblezone.dto.WaybleZoneDetailResponseDto;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.service.WaybleZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wayble-zones")
public class WaybleZoneController {

    private final WaybleZoneService waybleZoneService;

    @GetMapping
    @Operation(
            summary = "웨이블존 목록 조회",
            description = "city, category 파라미터를 기반으로 웨이블존 리스트를 조회합니다. " +
                    "각 웨이블존에 대해 이름, 주소, 평균 평점, 리뷰 수, 대표 이미지, 편의시설 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "웨이블존 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 category 파라미터",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 웨이블존에 대한 시설 정보가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = com.wayble.server.common.response.CommonResponse.class)))
    })
    public CommonResponse<List<WaybleZoneListResponseDto>> getWaybleZoneList(
            @RequestParam @NotBlank(message = "city는 필수입니다.") String city,
            @RequestParam @NotBlank(message = "category는 필수입니다.") String category
    ) {
        return CommonResponse.success(waybleZoneService.getWaybleZones(city, category));
    }

    @GetMapping("/{waybleZoneId}")
    @Operation(summary = "웨이블존 상세 조회", description = "웨이블존의 상세 정보를 조회합니다. (운영시간 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "웨이블존이 존재하지 않음")
    })
    public CommonResponse<WaybleZoneDetailResponseDto> getWaybleZoneDetail(
            @PathVariable @NotNull Long waybleZoneId
    ) {
        return CommonResponse.success(waybleZoneService.getWaybleZoneDetail(waybleZoneId));
    }
}