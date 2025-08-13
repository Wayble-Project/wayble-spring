package com.wayble.server.direction.external.opendata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StationSearchResponse( // 버스 정류장 id를 검색하는 엔드포인트
        StationSearchMsgBody msgBody
) {
    public record StationSearchMsgBody(
            List<StationItem> itemList
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StationItem(
            String stId,
            String stNm,
            String tmX,
            String tmY
    ) {}
}
