package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.facility.WaybleFacilityConditionDto;
import com.wayble.server.explore.dto.facility.WaybleFacilityResponseDto;
import com.wayble.server.explore.service.WaybleFacilityDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/facilities/search")
public class WaybleFacilitySearchController {
    private final WaybleFacilityDocumentService waybleFacilityDocumentService;

    @GetMapping("")
    public CommonResponse<List<WaybleFacilityResponseDto>> findNearbyFacilities(
            @Valid @ModelAttribute WaybleFacilityConditionDto conditionDto
    ) {
        return CommonResponse.success(waybleFacilityDocumentService.findNearbyFacilityDocuments(conditionDto));
    }
}
