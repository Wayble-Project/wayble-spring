package com.wayble.server.direction.service;

import com.wayble.server.direction.external.tmap.TMapClient;
import com.wayble.server.direction.external.tmap.dto.request.TMapRequest;
import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.external.tmap.dto.response.TMapResponse;
import com.wayble.server.direction.external.tmap.mapper.TMapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkingService {

    private final TMapClient tMapClient;
    private final TMapMapper tMapMapper;

    public TMapParsingResponse callTMapApi(TMapRequest request) {
        TMapResponse response = tMapClient.response(request);
        return tMapMapper.parseResponse(response);
    }
}
