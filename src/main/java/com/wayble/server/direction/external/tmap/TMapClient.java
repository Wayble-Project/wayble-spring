package com.wayble.server.direction.external.tmap;

import com.wayble.server.direction.external.tmap.dto.request.TMapRequest;
import com.wayble.server.direction.external.tmap.dto.response.TMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TMapClient {

    private final WebClient tMapWebClient;
    private final TMapProperties tMapProperties;

    public TMapResponse response(TMapRequest request) {
        return tMapWebClient.post()
                .uri("/tmap/routes/pedestrian?version=1")
                .header("appKey", tMapProperties.secretKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TMapResponse.class)
                .block();
    }
}
