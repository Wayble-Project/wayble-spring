package com.wayble.server.direction.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.controller.swagger.WalkingSwagger;
import com.wayble.server.direction.dto.response.WayblePathResponse;
import com.wayble.server.common.client.tmap.dto.request.TMapRequest;
import com.wayble.server.common.client.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.service.WalkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directions")
public class WalkingController implements WalkingSwagger {

    private final WalkingService walkingService;

    @Override
    @GetMapping("/walking")
    public CommonResponse<TMapParsingResponse> callTMapApi(
            @RequestParam double startX,
            @RequestParam double startY,
            @RequestParam double endX,
            @RequestParam double endY,
            @RequestParam String startName,
            @RequestParam String endName
    ) {
        TMapRequest request = new TMapRequest(startX, startY, endX, endY, startName, endName);
        return CommonResponse.success(walkingService.callTMapApi(request));
    }

    @Override
    @GetMapping("/wayble")
    public CommonResponse<WayblePathResponse> getWayblePath(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon
    ) {
        return CommonResponse.success(walkingService.findWayblePath(startLat, startLon, endLat, endLon));
    }
}
