package com.wayble.server.direction.controller.swagger;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.dto.response.WayblePathResponse;
import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[도보]", description = "도보 길찾기 관련 API")
public interface WalkingSwagger {
    @Operation(
            summary = "도보 최적 경로 길찾기 API",
            description = "T MAP API를 호출하여 도보 최적 경로 길찾기를 진행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "T MAP API 호출이 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<TMapParsingResponse> callTMapApi(
            @RequestParam double startX,
            @RequestParam double startY,
            @RequestParam double endX,
            @RequestParam double endY,
            @RequestParam String startName,
            @RequestParam String endName
    );

    @Operation(
            summary = "웨이블 추천 경로 길찾기 API",
            description = "웨이블 마커를 활용하여 웨이블 추천 경로 길찾기를 진행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "웨이블 추천 경로 길찾기가 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<WayblePathResponse> getWayblePath(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon
    );
}
