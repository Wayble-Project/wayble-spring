package com.wayble.server.common.client.tmap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tmap")
public record TMapProperties(
        String secretKey,
        String baseUrl
) {
}
