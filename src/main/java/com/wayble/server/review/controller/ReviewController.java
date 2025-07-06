package com.wayble.server.review.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 참고용 컨트롤러(지우셔도 돼요)
    @GetMapping("/hello")
    public CommonResponse<String> hello() {
        return CommonResponse.success("hello");
    }

    // 예외 사용 참고용 컨트롤러(지우셔도 돼요)
    @GetMapping("/ex")
    public CommonResponse<String> exception() {
        reviewService.makeException();
        return CommonResponse.success("예외 발생!");
    }
}
