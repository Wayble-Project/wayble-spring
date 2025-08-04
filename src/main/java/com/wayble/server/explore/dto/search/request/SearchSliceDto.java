package com.wayble.server.explore.dto.search.request;

import java.util.List;

public record SearchSliceDto<T>(
        List<T> content,
        boolean hasNext
) {
}
