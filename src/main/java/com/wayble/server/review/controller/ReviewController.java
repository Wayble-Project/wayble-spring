package com.wayble.server.review.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.review.dto.ReviewRegisterDto;
import com.wayble.server.review.dto.ReviewResponseDto;
import com.wayble.server.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wayble-zones/{waybleZoneId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "웨이블존 리뷰 작성", description = "웨이블존에 대한 리뷰를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리뷰 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 오류", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public CommonResponse<String> registerReview(
            @PathVariable Long waybleZoneId,
            @RequestBody @Valid ReviewRegisterDto dto,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader // 테스트를 위해 임시로 토큰 없이도 접근하도록 설정
    ) {
        reviewService.registerReview(waybleZoneId, dto, authorizationHeader);
        return CommonResponse.success("리뷰가 등록되었습니다.");
    }

    @GetMapping
    @Operation(summary = "웨이블존 리뷰 목록 조회", description = "웨이블존에 대한 리뷰를 최신순 또는 평점순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 웨이블존이 존재하지 않음")
    })
    public CommonResponse<List<ReviewResponseDto>> getReviews(
            @PathVariable Long waybleZoneId,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return CommonResponse.success(reviewService.getReviews(waybleZoneId, sort));
    }
}