package com.wayble.server.direction.external.opendata;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opendata.api")
public record OpenDataProperties(
        String key,
        String baseUrl,
        String encodedKey,
        Endpoints endpoints,
        int timeout,
        String userAgent,
        String accept
) {
    public record Endpoints(String arrivals, String stationByName) {}
}