package com.wayble.server.explore.repository;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.entity.WaybleZoneVisitLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WaybleZoneVisitLogDocumentRepository extends ElasticsearchRepository<WaybleZoneVisitLogDocument, Long>{
}
