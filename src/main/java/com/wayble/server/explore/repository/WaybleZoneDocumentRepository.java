package com.wayble.server.explore.repository;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface WaybleZoneDocumentRepository extends ElasticsearchRepository<WaybleZoneDocument, Long>{
    Optional<WaybleZoneDocument> findById(Long waybleZoneId);
    List<WaybleZoneDocument> findAll();
}
