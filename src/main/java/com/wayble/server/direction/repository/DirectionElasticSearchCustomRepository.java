package com.wayble.server.direction.repository;

import com.wayble.server.direction.dto.request.DirectionSearchRequest;
import com.wayble.server.direction.dto.response.DirectionSearchResponse;

import java.util.List;

public interface DirectionElasticSearchCustomRepository {
    List<DirectionSearchResponse> searchDirection(DirectionSearchRequest request);
}
