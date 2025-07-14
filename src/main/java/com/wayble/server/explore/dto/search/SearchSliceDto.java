package com.wayble.server.explore.dto.search;

import java.util.List;

public record SearchSliceDto<T>(
        List<T> content,
        boolean hasNext
) {
}
