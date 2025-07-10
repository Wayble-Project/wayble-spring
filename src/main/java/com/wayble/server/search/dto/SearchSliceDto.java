package com.wayble.server.search.dto;

import java.util.List;

public record SearchSliceDto<T>(
        List<T> content,
        boolean hasNext
) {
}
