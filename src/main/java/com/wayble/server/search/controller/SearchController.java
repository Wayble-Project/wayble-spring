package com.wayble.server.search.controller;

import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.search.entity.WaybleZoneDocument;
import com.wayble.server.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/test")
    public CommonResponse<WaybleZoneDocument> findEntity() {
        return CommonResponse.success(searchService.getWaybleZoneDocumentById(1L));
    }

    @PostMapping("/test")
    public CommonResponse<String> registerEntity() {
        searchService.save();
        return CommonResponse.success("테스트 객체 등록 완료!");
    }
}
