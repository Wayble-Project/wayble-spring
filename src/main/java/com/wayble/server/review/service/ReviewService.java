package com.wayble.server.review.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.review.dto.ReviewRegisterDto;
import com.wayble.server.review.dto.ReviewResponseDto;
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

import java.util.List;
import java.util.stream.Collectors;

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

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        Review review = Review.of(user, zone, dto.content(), dto.rating());
        reviewRepository.save(review);

        double newRating = ((zone.getRating() * zone.getReviewCount()) + dto.rating()) / (zone.getReviewCount() + 1);
        zone.updateRating(newRating);
        zone.addReviewCount(1);

        if (dto.images() != null) {
            for (String imageUrl : dto.images()) {
                reviewImageRepository.save(ReviewImage.of(review, imageUrl));
            }
        }
        waybleZoneRepository.save(zone);

        // visitDate 및 facilities 저장은 필요시 추가 구현
    }

    public List<ReviewResponseDto> getReviews(Long zoneId, String sort) {
        WaybleZone zone = waybleZoneRepository.findById(zoneId)
                .orElseThrow(() -> new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_NOT_FOUND));

        List<Review> reviews = switch (sort) {
            case "rating" -> reviewRepository.findByWaybleZoneIdOrderByRatingDesc(zoneId);
            case "latest" -> reviewRepository.findByWaybleZoneIdOrderByCreatedAtDesc(zoneId);
            default -> reviewRepository.findByWaybleZoneIdOrderByCreatedAtDesc(zoneId);
        };

        return reviews.stream()
                .map(review -> new ReviewResponseDto(
                        review.getId(),
                        review.getUser().getNickname(),
                        review.getRating(),
                        review.getContent(),
                        review.getCreatedAt().toLocalDate(),
                        review.getLikeCount(),
                        review.getReviewImageList().stream()
                                .map(ReviewImage::getImageUrl)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}