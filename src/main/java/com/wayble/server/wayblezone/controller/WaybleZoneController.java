package com.wayble.server.wayblezone.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.service.WaybleZoneService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wayble-zones")
public class WaybleZoneController {

    private final WaybleZoneService waybleZoneService;

    @GetMapping
    public CommonResponse<List<WaybleZoneListResponseDto>> getWaybleZoneList(
            @RequestParam @NotBlank(message = "city는 필수입니다.") String city,
            @RequestParam @NotBlank(message = "category는 필수입니다.") String category
    ) {
        return CommonResponse.success(waybleZoneService.getWaybleZones(city, category));
    }
}