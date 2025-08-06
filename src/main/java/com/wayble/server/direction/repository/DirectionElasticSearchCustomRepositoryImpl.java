package com.wayble.server.direction.repository;

import com.wayble.server.direction.dto.request.DirectionSearchRequest;
import com.wayble.server.direction.dto.response.DirectionSearchResponse;
import com.wayble.server.direction.entity.DirectionDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DirectionElasticSearchCustomRepositoryImpl implements DirectionElasticSearchCustomRepository{

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<DirectionSearchResponse> searchDirection(DirectionSearchRequest request) {
        if (request.name() == null || request.name().trim().isEmpty()) return List.of();
        
        Criteria criteria = new Criteria("name").matches(request.name());

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setMaxResults(5);

        return elasticsearchOperations
                .search(query, DirectionDocument.class)
                .map(SearchHit::getContent)
                .map(DirectionSearchResponse::from)
                .stream()
                .toList();
    }
}
