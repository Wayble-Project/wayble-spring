package com.wayble.server.common.config;

import com.wayble.server.common.client.tmap.TMapProperties;
import com.wayble.server.direction.external.kric.KricProperties;
import com.wayble.server.direction.external.opendata.OpenDataProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final TMapProperties tMapProperties;
    private final KricProperties kricProperties;
    private final OpenDataProperties openDataProperties;

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
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .filter((request, next) -> next.exchange(request)
                        .timeout(java.time.Duration.ofSeconds(15))
                        .retryWhen(reactor.util.retry.Retry.backoff(3, java.time.Duration.ofSeconds(1))
                                .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException)))
                .build();
    }

    @Bean
    public WebClient openDataWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(openDataProperties.baseUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(512 * 1024))
                .uriBuilderFactory(factory)
                .build();
    }
}
