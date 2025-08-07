package com.wayble.server.common.config;

import com.wayble.server.common.client.tmap.TMapProperties;
import com.wayble.server.direction.external.kric.KricProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final TMapProperties tMapProperties;
    private final KricProperties kricProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public WebClient tMapWebClient() {
        return WebClient.builder()
                .baseUrl(tMapProperties.baseUrl())
                .build();
    }

    @Bean
    public WebClient kricWebClient() {
        return WebClient.builder()
                .baseUrl(kricProperties.baseUrl())
                .build();
    }
}
