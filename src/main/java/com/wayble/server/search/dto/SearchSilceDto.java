package com.wayble.server.search.dto;

import java.util.List;

public record SearchSilceDto<T>(
        List<T> content,
        boolean hasNext
) {
}
