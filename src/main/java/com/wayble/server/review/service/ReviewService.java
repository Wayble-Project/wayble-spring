package com.wayble.server.review.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.review.exception.ReviewErrorCase;
import com.wayble.server.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public void makeException() {
        throw new ApplicationException(ReviewErrorCase.REVIEW_NOT_FOUND);
    }
}
