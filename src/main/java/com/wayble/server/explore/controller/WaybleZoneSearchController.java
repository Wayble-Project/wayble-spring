package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.search.request.SearchSliceDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneDistrictSearchDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.service.WaybleZoneSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/wayble-zones/search")
public class WaybleZoneSearchController {

    private final WaybleZoneSearchService waybleZoneSearchService;

    @GetMapping("/maps")
    public CommonResponse<SearchSliceDto<WaybleZoneSearchResponseDto>> findByCondition(
            @Valid @ModelAttribute WaybleZoneSearchConditionDto conditionDto,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size)
    {
        Slice<WaybleZoneSearchResponseDto> slice =
                waybleZoneSearchService.searchWaybleZonesByCondition(conditionDto, PageRequest.of(page, size));
        return CommonResponse.success(new SearchSliceDto<>(
                slice.getContent(),
                slice.hasNext()
        ));
    }

    @GetMapping("/district")
    public CommonResponse<List<WaybleZoneDistrictResponseDto>> findByDistrict(
            @Valid @ModelAttribute WaybleZoneDistrictSearchDto conditionDto)
    {
        return CommonResponse.success(waybleZoneSearchService.searchWaybleZonesByDistrict(
                conditionDto.district(), conditionDto.districtSearchType()
        ));
    }

    @PostMapping("")
    public CommonResponse<String> registerDocumentFromDto(@RequestBody WaybleZoneDocumentRegisterDto registerDto) {
        waybleZoneSearchService.saveDocumentFromDto(registerDto);
        return CommonResponse.success("Wayble Zone Document 등록 완료!");
    }
}
