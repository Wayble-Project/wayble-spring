package com.wayble.server.direction.external.opendata.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenDataResponse ( // 버스 정류장 id를 기반으로 배차시간, 저상버스 여부를 확인하는 엔드포인트
    @JsonProperty("comMsgHeader") ComMsgHeader comMsgHeader,
    @JsonProperty("msgHeader") MsgHeader msgHeader,
    @JsonProperty("msgBody") MsgBody msgBody
) {
    public record ComMsgHeader(
        @JsonProperty("errMsg") String errMsg,
        @JsonProperty("responseTime") String responseTime,
        @JsonProperty("requestMsgID") String requestMsgID,
        @JsonProperty("responseMsgID") String responseMsgID,
        @JsonProperty("successYN") String successYN,
        @JsonProperty("returnCode") String returnCode
    ) {}
    public record MsgHeader(
        @JsonProperty("headerMsg") String headerMsg,
        @JsonProperty("headerCd") String headerCd,
        @JsonProperty("itemCount") Integer itemCount
    ) {}
    
    public record MsgBody(
        @JsonProperty("itemList") List<Item> itemList
    ) {}
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
        @JsonProperty("busType1") String busType1,
        @JsonProperty("busType2") String busType2,
        @JsonProperty("term") String term,
        @JsonProperty("busRouteId") String busRouteId
    ) {}
}