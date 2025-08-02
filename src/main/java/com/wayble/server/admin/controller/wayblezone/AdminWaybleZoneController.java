package com.wayble.server.admin.controller.wayblezone;

import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneCreateDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneThumbnailDto;
import com.wayble.server.admin.dto.wayblezone.AdminWaybleZoneUpdateDto;
import com.wayble.server.admin.service.AdminWaybleZoneService;
import com.wayble.server.common.response.CommonResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/admin/wayble-zones")
public class AdminWaybleZoneController {

    private final AdminWaybleZoneService adminWaybleZoneService;

    @GetMapping()
    public CommonResponse<List<AdminWaybleZoneThumbnailDto>> findByCondition(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size)
    {
        return CommonResponse.success(adminWaybleZoneService.findWaybleZonesByPage(page, size));
    }

    @GetMapping("/count")
    public CommonResponse<Long> findAllWaybleZoneCount() {
        return CommonResponse.success(adminWaybleZoneService.getTotalWaybleZoneCounts());
    }

    @GetMapping("/{waybleZoneId}")
    public CommonResponse<Optional<AdminWaybleZoneDetailDto>> findWaybleZoneById(@PathVariable("waybleZoneId") long waybleZoneId) {
        return CommonResponse.success(adminWaybleZoneService.findWaybleZoneById(waybleZoneId));
    }
    
    @PostMapping
    public CommonResponse<Long> createWaybleZone(@Valid @RequestBody AdminWaybleZoneCreateDto createDto) {
        Long waybleZoneId = adminWaybleZoneService.createWaybleZone(createDto);
        return CommonResponse.success(waybleZoneId);
    }
    
    @PutMapping("/{waybleZoneId}")
    public CommonResponse<Long> updateWaybleZone(@PathVariable("waybleZoneId") Long waybleZoneId,
                                                @Valid @RequestBody AdminWaybleZoneUpdateDto updateDto) {
        // DTO의 ID와 URL의 ID가 일치하는지 확인
        AdminWaybleZoneUpdateDto validatedDto = new AdminWaybleZoneUpdateDto(
            waybleZoneId,
            updateDto.zoneName(),
            updateDto.contactNumber(),
            updateDto.zoneType(),
            updateDto.state(),
            updateDto.city(),
            updateDto.district(),
            updateDto.streetAddress(),
            updateDto.detailAddress(),
            updateDto.latitude(),
            updateDto.longitude(),
            updateDto.mainImageUrl()
        );
        
        Long updatedWaybleZoneId = adminWaybleZoneService.updateWaybleZone(validatedDto);
        return CommonResponse.success(updatedWaybleZoneId);
    }
}
