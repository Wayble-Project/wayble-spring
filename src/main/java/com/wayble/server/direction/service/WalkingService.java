package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.exception.WalkingErrorCase;
import com.wayble.server.direction.external.tmap.TMapClient;
import com.wayble.server.direction.external.tmap.dto.request.TMapRequest;
import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.external.tmap.dto.response.TMapResponse;
import com.wayble.server.direction.external.tmap.mapper.TMapMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkingService {

    private final TMapClient tMapClient;
    private final TMapMapper tMapMapper;

    public TMapParsingResponse callTMapApi(TMapRequest request) {
        try {
            TMapResponse response = tMapClient.response(request);
            log.info("🎉 T MAP API 호출 성공");
            return tMapMapper.parseResponse(response);
        } catch (Exception e) {
            log.error("🚨 T MAP API 호출 실패: {}", e.getMessage());
            throw new ApplicationException(WalkingErrorCase.T_MAP_API_FAILED);
        }
    }
}
