package com.wayble.server.admin.controller;

import com.wayble.server.admin.service.AdminUserService;
import com.wayble.server.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    @GetMapping("/count")
    public CommonResponse<Long> getTotalUserCount() {
        return CommonResponse.success(adminUserService.getTotalUserCount());
    }
}