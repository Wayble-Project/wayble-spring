package com.wayble.server.direction.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.controller.swagger.DirectionSwagger;
import com.wayble.server.direction.dto.request.PlaceSaveRequest;
import com.wayble.server.direction.dto.response.DirectionSearchResponse;
import com.wayble.server.direction.service.DirectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directions")
public class DirectionController implements DirectionSwagger {

    private final DirectionService directionService;

    @Override
    @PostMapping("/place")
    public CommonResponse<String> savePlace(@RequestBody @Valid PlaceSaveRequest request) {
        directionService.savePlaceAndIndexDocument(request.requests());
        return CommonResponse.success("Place Save successful");
    }

    @Override
    @GetMapping("/keywords")
    public CommonResponse<List<DirectionSearchResponse>> getDirectionKeywords(@RequestParam String keyword) {
        return CommonResponse.success(directionService.searchDirection(keyword));
    }
}
