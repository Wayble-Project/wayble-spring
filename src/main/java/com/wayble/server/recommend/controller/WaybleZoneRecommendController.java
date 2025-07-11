package com.wayble.server.recommend.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.recommend.dto.WaybleZoneRecommendResponseDto;
import com.wayble.server.recommend.service.WaybleZoneRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/wayble-zones/recommend")
public class WaybleZoneRecommendController {

    private final WaybleZoneRecommendService waybleZoneRecommendService;

    @GetMapping("/{userId}")
    public CommonResponse<WaybleZoneRecommendResponseDto> getWaybleZonePersonalRecommend(
            @PathVariable("userId") Long userId) {
        WaybleZoneRecommendResponseDto dto = waybleZoneRecommendService.getWaybleZonePersonalRecommend(userId);
        return CommonResponse.success(dto);
    }
}
