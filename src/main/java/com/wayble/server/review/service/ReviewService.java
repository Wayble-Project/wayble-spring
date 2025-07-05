package com.wayble.server.review.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.review.dto.ReviewRegisterDto;
import com.wayble.server.review.entity.Review;
import com.wayble.server.review.entity.ReviewImage;
import com.wayble.server.review.repository.ReviewImageRepository;
import com.wayble.server.review.repository.ReviewRepository;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.exception.WaybleZoneErrorCase;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final WaybleZoneRepository waybleZoneRepository;
    private final UserRepository userRepository;

    public void registerReview(Long zoneId, ReviewRegisterDto dto, String token) {
        WaybleZone zone = waybleZoneRepository.findById(zoneId)
                .orElseThrow(() -> new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_NOT_FOUND));

        User user = userRepository.findById(dto.user_id())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        Review review = Review.builder()
                .waybleZone(zone)
                .user(user)
                .content(dto.content())
                .rating(dto.rating())
                .build();

        reviewRepository.save(review);

        if (dto.images() != null) {
            for (String imageUrl : dto.images()) {
                reviewImageRepository.save(ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrl)
                        .build());
            }
        }

        // facilities 및 visit_date 저장 기능 필요 시 추가 구현
    }
}