package com.wayble.server.recommend.repository;

import com.wayble.server.search.entity.WaybleZoneDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WaybleZoneRecommendRepository extends ElasticsearchRepository<WaybleZoneDocument, Long>, WaybleZoneSearchRepositoryCustom {
}
