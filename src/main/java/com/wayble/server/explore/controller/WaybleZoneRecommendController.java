package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.service.WaybleZoneRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/wayble-zones/recommend")
public class WaybleZoneRecommendController {

    private final WaybleZoneRecommendService waybleZoneRecommendService;

    @GetMapping()
    public CommonResponse<List<WaybleZoneRecommendResponseDto>> getWaybleZonePersonalRecommend(
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(name = "count", defaultValue = "1") int count) {
        List<WaybleZoneRecommendResponseDto> result = waybleZoneRecommendService.getWaybleZonePersonalRecommend(userId, latitude, longitude, count);
        return CommonResponse.success(result);
    }
}
