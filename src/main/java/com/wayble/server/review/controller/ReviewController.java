package com.wayble.server.review.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.review.dto.ReviewRegisterDto;
import com.wayble.server.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wayble-zones/{waybleZoneId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "웨이블존 리뷰 작성", description = "웨이블존에 대한 리뷰를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public CommonResponse<String> registerReview(
            @PathVariable Long waybleZoneId,
            @RequestBody @Valid ReviewRegisterDto dto,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        reviewService.registerReview(waybleZoneId, dto, authorizationHeader);
        return CommonResponse.success("리뷰가 등록되었습니다.");
    }
}