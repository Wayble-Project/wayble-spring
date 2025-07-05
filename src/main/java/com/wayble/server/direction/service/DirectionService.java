package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.review.exception.ReviewErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectionService {

    public void makeException() {
        throw new ApplicationException(ReviewErrorCase.REVIEW_NOT_FOUND);
    }
}
