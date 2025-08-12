package com.wayble.server.explore.repository.facility;

import com.wayble.server.explore.entity.WaybleFacilityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface WaybleFacilityDocumentRepository extends ElasticsearchRepository<WaybleFacilityDocument, String> {
    List<WaybleFacilityDocument> findAll();
}
