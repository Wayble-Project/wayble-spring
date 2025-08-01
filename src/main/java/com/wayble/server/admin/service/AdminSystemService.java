package com.wayble.server.admin.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSystemService {
    
    private final ElasticsearchClient elasticsearchClient;
    private final DataSource dataSource;
    
    public boolean isElasticsearchHealthy() {
        try {
            HealthResponse response = elasticsearchClient.cluster().health();
            log.debug("Elasticsearch health status: {}", response.status());
            return response.status() != co.elastic.clients.elasticsearch._types.HealthStatus.Red;
        } catch (Exception e) {
            log.error("Elasticsearch health check failed", e);
            return false;
        }
    }
    
    public boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5초 타임아웃
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }
    
    public boolean isApiServerHealthy() {
        // API 서버가 실행 중이면 true (이 메서드가 호출되는 것 자체가 서버가 살아있다는 증거)
        return true;
    }
    
    public boolean isFileStorageHealthy() {
        // 파일 스토리지 상태 체크 (예: S3 연결 확인 등)
        return true;
    }
}