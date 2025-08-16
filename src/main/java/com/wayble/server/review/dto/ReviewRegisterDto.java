package com.wayble.server.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "리뷰 등록 요청 DTO")
public record ReviewRegisterDto(
        @Schema(description = "리뷰 내용", example = "뷰가 좋고 접근성이 좋은 카페예요.")
        @NotBlank(message = "리뷰 내용은 비어 있을 수 없습니다.")
        String content,

        @Schema(description = "평점", example = "5.0")
        @NotNull(message = "평점은 필수입니다.")
        @DecimalMin(value = "0.0", message = "평점은 0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5 이하여야 합니다.")
        Double rating,

        @Schema(description = "방문 날짜", example = "2025-06-30")
        @NotNull(message = "방문 날짜는 필수입니다.")
        LocalDate visitDate,

        @Schema(description = "시설 정보", example = "[\"주차장 있음\", \"장애인 화장실 있음\"]")
        @NotNull(message = "시설 정보는 필수입니다.")
        List<String> facilities,

        @Schema(description = "리뷰 이미지 리스트", example = "[\"https://image.url/review1.jpg\"]")
        List<String> images

) {}