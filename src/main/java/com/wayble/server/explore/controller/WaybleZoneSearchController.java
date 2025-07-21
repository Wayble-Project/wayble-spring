package com.wayble.server.explore.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.explore.dto.search.SearchSliceDto;
import com.wayble.server.explore.dto.search.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.dto.search.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.service.WaybleZoneSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("")
    public CommonResponse<String> registerDocumentFromDto(@RequestBody WaybleZoneDocumentRegisterDto registerDto) {
        waybleZoneSearchService.saveDocumentFromDto(registerDto);
        return CommonResponse.success("Wayble Zone Document 등록 완료!");
    }
}
