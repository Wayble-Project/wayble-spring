package com.wayble.server.direction.external.kric;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kric.api")
public record KricProperties(
        String key,
        String baseUrl
) {
}