package com.wayble.server.explore.repository;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WaybleZoneVisitLogDocumentRepository extends ElasticsearchRepository<WaybleZoneDocument, Long>{
}
