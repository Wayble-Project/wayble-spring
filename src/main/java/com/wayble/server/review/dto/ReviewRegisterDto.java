package com.wayble.server.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "리뷰 등록 요청 DTO")
public record ReviewRegisterDto(

        @Schema(description = "작성자 ID", example = "1")
        Long user_id,

        @Schema(description = "리뷰 내용", example = "뷰가 좋고 접근성이 좋은 카페예요.")
        String content,

        @Schema(description = "평점", example = "5.0")
        @DecimalMin("0.0") @DecimalMax("5.0")
        Double rating,

        @Schema(description = "방문 날짜", example = "2025-06-30")
        LocalDate visit_date,

        @Schema(description = "시설 정보", example = "[\"주차장 있음\", \"장애인 화장실 있음\"]")
        List<String> facilities,

        @Schema(description = "리뷰 이미지 리스트", example = "[\"https://image.url/review1.jpg\"]")
        List<String> images

) {}