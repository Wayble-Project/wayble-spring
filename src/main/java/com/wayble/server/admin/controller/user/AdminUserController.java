package com.wayble.server.admin.controller.user;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.admin.service.AdminUserService;
import com.wayble.server.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    @GetMapping("/count")
    public CommonResponse<Long> getTotalUserCount() {
        return CommonResponse.success(adminUserService.getTotalUserCount());
    }
    
    @GetMapping()
    public CommonResponse<List<AdminUserThumbnailDto>> findByCondition(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size)
    {
        return CommonResponse.success(adminUserService.findUsersByPage(page, size));
    }
    
    @GetMapping("/{userId}")
    public CommonResponse<Optional<AdminUserDetailDto>> findUserById(@PathVariable("userId") long userId) {
        return CommonResponse.success(adminUserService.findUserById(userId));
    }
    
    @GetMapping("/deleted/count")
    public CommonResponse<Long> getTotalDeletedUserCount() {
        return CommonResponse.success(adminUserService.getTotalDeletedUserCount());
    }
    
    @GetMapping("/deleted")
    public CommonResponse<List<AdminUserThumbnailDto>> findDeletedUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size)
    {
        return CommonResponse.success(adminUserService.findDeletedUsersByPage(page, size));
    }
    
    @GetMapping("/deleted/{userId}")
    public CommonResponse<Optional<AdminUserDetailDto>> findDeletedUserById(@PathVariable("userId") long userId) {
        return CommonResponse.success(adminUserService.findDeletedUserById(userId));
    }
}