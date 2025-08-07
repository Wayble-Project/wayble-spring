package com.wayble.server.logging.repository;

import com.wayble.server.logging.entity.UserActionLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActionLogRepository extends ElasticsearchRepository<UserActionLog, String> {
    
    List<UserActionLog> findByActionAndTimestampBetween(String action, LocalDateTime start, LocalDateTime end);
    
    List<UserActionLog> findByUserIdAndActionAndTimestampBetween(Long userId, String action, LocalDateTime start, LocalDateTime end);
    
    long countByActionAndTimestampBetween(String action, LocalDateTime start, LocalDateTime end);
    
    long countByUserIdAndActionAndTimestampBetween(Long userId, String action, LocalDateTime start, LocalDateTime end);
    
    long countDistinctUserIdByActionAndTimestampBetween(String action, LocalDateTime start, LocalDateTime end);
}