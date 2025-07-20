package com.wayble.server.direction.external.tmap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "도보 최적 경로 T MAP API")
public record TMapParsingResponse(
        @Schema(description = "총 거리", example = "909")
        int totalDistance,

        @Schema(description = "총 소요 시간", example = "676")
        int totalTime,

        @Schema(
                description = "경로 단계",
                example = "[{\"type\":\"point\",\"name\":\"강남역\",\"description\":\"보행자도로를 따라 32m 이동\"}]"
        )
        List<Step> steps
) {

    @Schema(description = "경로 단계")
    public record Step(
            @Schema(description = "단계 타입 (point - 지점, line - 도로)", example = "point")
            String type,

            @Schema(description = "지점 또는 도로명", example = "강남역")
            String name,

            @Schema(
                    description = "설명",
                    example = "강남역에서 좌측 횡단보도를 건넌 후 보행자도로를 따라 32m 이동"
            )
            String description,

            @Schema(
                    description = "해당 포인트 좌표 (type - point)",
                    example = "{\"longitude\":127.0241571,\"latitude\":37.5037355}"
            )
            Coordinate coordinate,

            @Schema(
                    description = "좌표 리스트 (type - line)",
                    example = "[{\"longitude\":127.0241571,\"latitude\":37.5037355},{\"longitude\":127.0315678,\"latitude\":37.5067709}]"
            )
            List<Coordinate> coordinates,

            @Schema(description = "턴 타입", example = "212")
            Integer turnType,

            @Schema(
                    description = "포인트 타입 (SP - 출발, EP - 도착, GP - 경유지)",
                    example = "SP"
            )
            String pointType,

            @Schema(description = "해당 구간 거리", example = "32")
            Integer distance,

            @Schema(description = "해당 구간 소요 시간", example = "21")
            Integer time
    ) {}

    @Schema(description = "좌표")
    public record Coordinate(
            @Schema(description = "경도", example = "127.02415714643489")
            double longitude,

            @Schema(description = "위도", example = "37.503735591581")
            double latitude
    ) {}
}
