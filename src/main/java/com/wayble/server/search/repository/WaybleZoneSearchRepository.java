package com.wayble.server.search.repository;

import com.wayble.server.search.entity.WaybleZoneDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface WaybleZoneSearchRepository extends ElasticsearchRepository<WaybleZoneDocument, Long> {
    Optional<WaybleZoneDocument> findById(Long waybleZoneId);
}
