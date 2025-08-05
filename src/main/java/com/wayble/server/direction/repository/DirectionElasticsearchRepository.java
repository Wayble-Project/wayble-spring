package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.DirectionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DirectionElasticsearchRepository extends ElasticsearchRepository<DirectionDocument, Long>, DirectionElasticSearchCustomRepository {
}
