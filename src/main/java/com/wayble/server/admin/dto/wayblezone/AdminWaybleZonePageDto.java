package com.wayble.server.admin.dto.wayblezone;

import java.util.List;

public record AdminWaybleZonePageDto(
        List<AdminWaybleZoneThumbnailDto> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static AdminWaybleZonePageDto of(
            List<AdminWaybleZoneThumbnailDto> content,
            int currentPage,
            int pageSize,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean hasNext = currentPage < totalPages - 1;
        boolean hasPrevious = currentPage > 0;
        
        return new AdminWaybleZonePageDto(
                content,
                currentPage,
                pageSize,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }
}