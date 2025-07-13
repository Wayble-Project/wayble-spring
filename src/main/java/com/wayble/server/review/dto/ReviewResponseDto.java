package com.wayble.server.review.dto;

import java.time.LocalDate;
import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        String userNickname,
        double rating,
        String content,
        LocalDate visitDate,
        int likes,
        List<String> images
) {}
