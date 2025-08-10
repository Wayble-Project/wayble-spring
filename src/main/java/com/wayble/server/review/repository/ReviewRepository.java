package com.wayble.server.review.repository;

import com.wayble.server.review.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user", "reviewImageList"})
    List<Review> findByWaybleZoneIdOrderByCreatedAtDesc(Long waybleZoneId); // 최신순

    @EntityGraph(attributePaths = {"user", "reviewImageList"})
    List<Review> findByWaybleZoneIdOrderByRatingDesc(Long waybleZoneId); // 평점순
}
