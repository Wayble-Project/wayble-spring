package com.wayble.server.admin.dto.user;

import java.util.List;

public record AdminUserPageDto(
    List<AdminUserThumbnailDto> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious
) {
    
    public static AdminUserPageDto of(List<AdminUserThumbnailDto> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return new AdminUserPageDto(
            content, page, size, totalElements, totalPages, 
            first, last, hasNext, hasPrevious
        );
    }
}