package com.wayble.server.admin.controller;

import com.wayble.server.admin.dto.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.AdminWaybleZoneThumbnailDto;
import com.wayble.server.admin.service.AdminWaybleZoneService;
import com.wayble.server.common.response.CommonResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/admin/wayble-zones")
public class AdminWaybleZoneController {

    private final AdminWaybleZoneService adminWaybleZoneService;

    @GetMapping()
    public CommonResponse<List<AdminWaybleZoneThumbnailDto>> findByCondition(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size)
    {
        return CommonResponse.success(adminWaybleZoneService.findWaybleZonesByPage(page, size));
    }

    @GetMapping("/count")
    public CommonResponse<Long> findAllWaybleZoneCount() {
        return CommonResponse.success(adminWaybleZoneService.getTotalWaybleZoneCounts());
    }

    @GetMapping("/{waybleZoneId}")
    public CommonResponse<Optional<AdminWaybleZoneDetailDto>> findWaybleZoneById(@PathVariable("waybleZoneId") long waybleZoneId) {
        return CommonResponse.success(adminWaybleZoneService.findWaybleZoneById(waybleZoneId));
    }
}
