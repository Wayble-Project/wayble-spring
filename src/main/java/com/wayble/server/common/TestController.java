package com.wayble.server.common;

import com.wayble.server.common.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * @return
     * {
     *     "data": "test"
     * }
     */

    @GetMapping("")
    public CommonResponse<String> test() {
        return CommonResponse.success("test");
    }
}
