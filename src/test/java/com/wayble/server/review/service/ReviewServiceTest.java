package com.wayble.server.review.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.review.dto.ReviewRegisterDto;
import com.wayble.server.review.entity.Review;
import com.wayble.server.review.entity.ReviewImage;
import com.wayble.server.review.repository.ReviewImageRepository;
import com.wayble.server.review.repository.ReviewRepository;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final ReviewImageRepository reviewImageRepository = mock(ReviewImageRepository.class);
    private final WaybleZoneRepository waybleZoneRepository = mock(WaybleZoneRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final ReviewService sut =
            new ReviewService(reviewRepository, reviewImageRepository, waybleZoneRepository, userRepository);

    @Test
    @DisplayName("리뷰 등록 성공 - 평점 갱신, 카운트 증가, 이미지 저장")
    void t1() {
        Long zoneId = 10L;
        Long userId = 5L;

        WaybleZone zone = mock(WaybleZone.class);
        when(waybleZoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(zone.getRating()).thenReturn(4.0);
        when(zone.getReviewCount()).thenReturn(1L);

        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ReviewRegisterDto dto = new ReviewRegisterDto(
                "뷰가 좋고 접근성이 좋아요",
                5.0,
                LocalDate.of(2025, 6, 30),
                List.of("주차장 있음", "장애인 화장실 있음"),
                List.of("https://image.url/review1.jpg")
        );

        doAnswer(invocation -> invocation.getArgument(0))
                .when(reviewRepository).save(any(Review.class));

        sut.registerReview(zoneId, userId, dto);


        verify(reviewRepository, times(1)).save(any(Review.class));

        ArgumentCaptor<Double> ratingCaptor = ArgumentCaptor.forClass(Double.class);
        verify(zone, times(1)).updateRating(ratingCaptor.capture());

        assertEquals(4.5, ratingCaptor.getValue(), 1e-6);

        verify(zone, times(1)).addReviewCount(1);
        verify(reviewImageRepository, times(1)).save(any(ReviewImage.class));
        verify(waybleZoneRepository, times(1)).save(zone);
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 웨이블존 없음")
    void t2() {
        Long zoneId = 99L;
        Long userId = 1L;
        when(waybleZoneRepository.findById(zoneId)).thenReturn(Optional.empty());

        ReviewRegisterDto dto = new ReviewRegisterDto(
                "좋아요", 4.0, LocalDate.now(), List.of("주차장"), List.of()
        );

        assertThrows(ApplicationException.class,
                () -> sut.registerReview(zoneId, userId, dto));
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 유저 없음")
    void t3() {
        Long zoneId = 10L;
        Long userId = 999L;

        WaybleZone zone = mock(WaybleZone.class);
        when(waybleZoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ReviewRegisterDto dto = new ReviewRegisterDto(
                "좋아요", 4.0, LocalDate.now(), List.of("주차장"), List.of()
        );

        assertThrows(ApplicationException.class,
                () -> sut.registerReview(zoneId, userId, dto));
    }
}