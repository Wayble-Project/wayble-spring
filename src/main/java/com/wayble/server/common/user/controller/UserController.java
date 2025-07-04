package com.wayble.server.common.user.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.common.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public CommonResponse<String> hello() {
        return CommonResponse.success("hello");
    }
}
