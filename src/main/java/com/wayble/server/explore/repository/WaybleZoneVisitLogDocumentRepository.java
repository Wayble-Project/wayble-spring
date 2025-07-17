package com.wayble.server.explore.repository;

import com.wayble.server.explore.entity.WaybleZoneVisitLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface WaybleZoneVisitLogDocumentRepository extends ElasticsearchRepository<WaybleZoneVisitLogDocument, String>{
    List<WaybleZoneVisitLogDocument> findAll();
}

