package com.wayble.server.search.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.search.dto.WaybleZoneDocumentRegisterDto;
import com.wayble.server.search.dto.WaybleZoneSearchConditionDto;
import com.wayble.server.search.dto.WaybleZoneSearchResponseDto;
import com.wayble.server.search.entity.WaybleZoneDocument;
import com.wayble.server.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/test")
    public CommonResponse<WaybleZoneDocument> findEntity() {
        return CommonResponse.success(searchService.getWaybleZoneDocumentById(1L));
    }

    @GetMapping("")
    public CommonResponse<List<WaybleZoneSearchResponseDto>> findByCondition(@ModelAttribute WaybleZoneSearchConditionDto conditionDto) {
        return CommonResponse.success(searchService.searchWaybleZonesByCondition(conditionDto));
    }

    @PostMapping("")
    public CommonResponse<String> registerDocumentFromDto(@RequestBody WaybleZoneDocumentRegisterDto registerDto) {
        searchService.saveDocumentFromDto(registerDto);
        return CommonResponse.success("Wayble Zone Document 등록 완료!");
    }
}
