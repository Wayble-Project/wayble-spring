package com.wayble.server.explore.repository;

import com.wayble.server.explore.entity.RecommendLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface RecommendLogDocumentRepository extends ElasticsearchRepository<RecommendLogDocument, Long> {
    Optional<RecommendLogDocument> findByUserIdAndZoneId(Long userId, Long zoneId);

    Boolean existsByUserIdAndZoneId(Long userId, Long zoneId);
}
