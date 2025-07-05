package com.wayble.server.direction.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directions")
public class DirectionController {

    private final DirectionService directionService;

    // 참고용 컨트롤러(지우셔도 돼요)
    @GetMapping("/hello")
    public CommonResponse<String> hello() {
        return CommonResponse.success("hello");
    }

    // 예외 사용 참고용 컨트롤러(지우셔도 돼요)
    @GetMapping("/ex")
    public CommonResponse<String> exception() {
        directionService.makeException();
        return CommonResponse.success("예외 발생!");
    }
}
