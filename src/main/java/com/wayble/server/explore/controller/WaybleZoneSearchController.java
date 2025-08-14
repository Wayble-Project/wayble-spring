package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.search.request.SearchSliceDto;
import com.wayble.server.wayblezone.dto.WaybleZoneRegisterDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.service.WaybleZoneDocumentService;
import com.wayble.server.explore.service.WaybleZoneSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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

    private final WaybleZoneDocumentService waybleZoneDocumentService;

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

    @GetMapping("/district/most-searches")
    public CommonResponse<List<WaybleZoneDistrictResponseDto>> findMostSearchesWaybleZoneByDistrict(
            @Valid
            @RequestParam("district")
            @Size(min = 2, message = "동 이름은 최소 2글자 이상이어야 합니다.")
            String district)
    {
        return CommonResponse.success(waybleZoneSearchService.searchMostSearchesWaybleZoneByDistrict(
                district
        ));
    }

    @GetMapping("/district/most-likes")
    public CommonResponse<List<WaybleZoneDistrictResponseDto>> findMostLikesWaybleZoneByDistrict(
            @Valid
            @RequestParam("district")
            @Size(min = 2, message = "동 이름은 최소 2글자 이상이어야 합니다.")
            String district
        )
    {
        return CommonResponse.success(waybleZoneSearchService.searchMostLikesWaybleZoneByDistrict(
                district
        ));
    }

    @GetMapping("/validate")
    public CommonResponse<WaybleZoneSearchResponseDto> findIsValidWaybleZone(
            @Valid @ModelAttribute WaybleZoneSearchConditionDto conditionDto
    )
    {
        return CommonResponse.success(waybleZoneSearchService.isValidWaybleZone(conditionDto));
    }

    @PostMapping("")
    public CommonResponse<String> registerDocumentFromDto(@RequestBody WaybleZoneRegisterDto registerDto) {
        waybleZoneDocumentService.saveDocumentFromDto(registerDto);
        return CommonResponse.success("Wayble Zone Document 등록 완료!");
    }
}
