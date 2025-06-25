package com.wayble.server.common;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.common.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main")
public class MainController {

    /**
     * @return
     * {
     *     "data": "main"
     * }
     */

    @GetMapping("")
    public CommonResponse<String> mainTest() {
        return CommonResponse.success("main");
    }

    /**
     * @return
     * {
     *     "errorCode": 1001,
     *     "message": "예외 메시지"
     * }
     */
    @GetMapping("/exception")
    public CommonResponse<String> exceptionTest() {
        throw new ApplicationException(MainErrorCase.MAIN_TEST_ERROR);
    }
}
