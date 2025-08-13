package com.wayble.server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

    @Value("${http.client.connect-timeout:10}")
    private int connectTimeout;

    @Value("${http.client.request-timeout:30}")
    private int requestTimeout;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
    }
}
