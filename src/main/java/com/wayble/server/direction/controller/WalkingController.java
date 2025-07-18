package com.wayble.server.direction.controller;

import com.wayble.server.direction.external.tmap.dto.request.TMapRequest;
import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.service.WalkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directions/walking")
public class WalkingController {

    private final WalkingService walkingService;

    @GetMapping()
    public TMapParsingResponse callTMapApi(
            @RequestParam double startX,
            @RequestParam double startY,
            @RequestParam double endX,
            @RequestParam double endY,
            @RequestParam String startName,
            @RequestParam String endName
    ) {
        TMapRequest request = new TMapRequest(startX, startY, endX, endY, startName, endName);
        return walkingService.callTMapApi(request);
    }
}
