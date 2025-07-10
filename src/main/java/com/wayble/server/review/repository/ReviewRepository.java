package com.wayble.server.review.repository;

import com.wayble.server.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByWaybleZoneIdOrderByCreatedAtDesc(Long waybleZoneId); // 최신순
    List<Review> findByWaybleZoneIdOrderByRatingDesc(Long waybleZoneId); // 평점순
}
