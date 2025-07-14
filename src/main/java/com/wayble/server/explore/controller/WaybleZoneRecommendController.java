package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.service.WaybleZoneRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/wayble-zones/recommend")
public class WaybleZoneRecommendController {

    private final WaybleZoneRecommendService waybleZoneRecommendService;

    @GetMapping()
    public CommonResponse<WaybleZoneRecommendResponseDto> getWaybleZonePersonalRecommend(
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {
        WaybleZoneRecommendResponseDto dto = waybleZoneRecommendService.getWaybleZonePersonalRecommend(userId, latitude, longitude);
        return CommonResponse.success(dto);
    }
}
