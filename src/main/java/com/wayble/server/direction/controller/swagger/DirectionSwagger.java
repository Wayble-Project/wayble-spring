package com.wayble.server.direction.controller.swagger;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.dto.request.PlaceSaveRequest;
import com.wayble.server.direction.dto.response.DirectionSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "[길찾기 - 검색]", description = "길찾기 검색 등 API")
public interface DirectionSwagger {
    @Operation(
            summary = "길찾기에서 검색한 장소 저장 및 place -> directionDocument 변환 API",
            description = "네이버 검색 API 호출 후, 검색한 장소를 저장 및 변환 + 색인 과정을 진행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "장소 저장 및 변환이 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<String> savePlace(
            @RequestBody @Valid PlaceSaveRequest request
    );

    @Operation(
            summary = "길찾기 검색 연관어 자동완성 API",
            description = "길찾기 장소 검색 시, 연관어 자동완성을 진행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "길찾기 검색 연관어 자동완성이 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<List<DirectionSearchResponse>> getDirectionKeywords(
            @RequestParam String keyword
    );
}
