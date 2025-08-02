package com.wayble.server.admin.dto;

public record SystemStatusDto(
    boolean apiServerStatus,
    boolean databaseStatus,
    boolean elasticsearchStatus,
    boolean fileStorageStatus
) {
    
    public static SystemStatusDto of(boolean apiServer, boolean database, boolean elasticsearch, boolean fileStorage) {
        return new SystemStatusDto(apiServer, database, elasticsearch, fileStorage);
    }
}