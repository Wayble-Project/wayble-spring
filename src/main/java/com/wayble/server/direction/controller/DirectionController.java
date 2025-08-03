package com.wayble.server.direction.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.direction.dto.request.PlaceSaveRequest;
import com.wayble.server.direction.service.DirectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/directions")
public class DirectionController {

    private final DirectionService directionService;

    @PostMapping("/place")
    public CommonResponse<String> savePlace(@RequestBody @Valid PlaceSaveRequest request) {
        directionService.savePlace(request);
        return CommonResponse.success("Place Save successful");
    }
}
