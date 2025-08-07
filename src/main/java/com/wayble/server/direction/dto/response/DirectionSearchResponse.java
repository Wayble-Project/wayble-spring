package com.wayble.server.direction.dto.response;

import com.wayble.server.direction.entity.DirectionDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "지도 연관어 자동완성 API")
public record DirectionSearchResponse(
        @Schema(description = "장소의 고유 ID", example = "1")
        Long placeId,

        @Schema(description = "장소의 이름", example = "아임히어")
        String name,

        @Schema(description = "주소", example = "서울시 용산구 청파동")
        String address,

        @Schema(description = "위도", example = "37.84512")
        Double latitude,

        @Schema(description = "경도", example = "127.87451")
        Double longitude
) {
    public static DirectionSearchResponse from(DirectionDocument directionDocument) {
        return DirectionSearchResponse.builder()
                .placeId(directionDocument.getId())
                .name(directionDocument.getName())
                .address(directionDocument.getEsAddress().getStreetAddress())
                .latitude(directionDocument.getEsAddress().getLocation().getLat())
                .longitude(directionDocument.getEsAddress().getLocation().getLon())
                .build();
    }
}
