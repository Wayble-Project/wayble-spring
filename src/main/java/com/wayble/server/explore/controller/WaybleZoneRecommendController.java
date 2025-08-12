package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendConditionDto;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.service.WaybleZoneRecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @Valid @ModelAttribute WaybleZoneRecommendConditionDto conditionDto,
            @RequestParam(name = "size", defaultValue = "1") int size) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<WaybleZoneRecommendResponseDto> result = waybleZoneRecommendService.getWaybleZonePersonalRecommend(
                userId,
                conditionDto.latitude(),
                conditionDto.longitude(),
                size
        );
        return CommonResponse.success(result);
    }
}
