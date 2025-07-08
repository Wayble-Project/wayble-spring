package com.wayble.server.search.repository;

import com.wayble.server.search.entity.WaybleZoneDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface WaybleZoneSearchRepository extends ElasticsearchRepository<WaybleZoneDocument, Long>, WaybleZoneSearchRepositoryCustom{
    Optional<WaybleZoneDocument> findById(Long waybleZoneId);
    List<WaybleZoneDocument> findAll();
}
