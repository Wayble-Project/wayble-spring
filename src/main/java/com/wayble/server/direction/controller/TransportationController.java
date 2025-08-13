package com.wayble.server.direction.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.dto.request.TransportationRequestDto;
import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.service.TransportationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directions/transportation")
@Tag(name = "Directions")
public class TransportationController {
    private final TransportationService transportationService;

    @Operation(
            summary = "대중교통 경로 조회",
            description = "출발지와 도착지 정보를 받아 최적의 대중교통 경로를 제공합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransportationRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "경로 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = TransportationResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "4001",
                            description = "경로를 찾을 수 없습니다.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류입니다.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/")
    public CommonResponse<TransportationResponseDto> findDirections(
            @RequestBody TransportationRequestDto request
    ){
        TransportationResponseDto data = transportationService.findRoutes(request);
        return CommonResponse.success(data);
    }

}
