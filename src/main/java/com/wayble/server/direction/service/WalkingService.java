package com.wayble.server.direction.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.direction.dto.response.WayblePathResponse;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.exception.WalkingErrorCase;
import com.wayble.server.direction.external.tmap.TMapClient;
import com.wayble.server.direction.external.tmap.dto.request.TMapRequest;
import com.wayble.server.direction.external.tmap.dto.response.TMapParsingResponse;
import com.wayble.server.direction.external.tmap.dto.response.TMapResponse;
import com.wayble.server.direction.external.tmap.mapper.TMapMapper;
import com.wayble.server.direction.init.GraphInit;
import com.wayble.server.direction.service.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkingService {

    private final TMapClient tMapClient;
    private final TMapMapper tMapMapper;
    private final GraphInit graphInit;
    private final WaybleDijkstraService waybleDijkstraService;

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

    public WayblePathResponse findWayblePath(
            double startLat,
            double startLon,
            double endLat,
            double endLon
    ) {
        long startNode = findNearestNode(startLat, startLon);
        long endNode = findNearestNode(endLat, endLon);

        return waybleDijkstraService.createWayblePath(startNode, endNode);
    }

    private long findNearestNode(double lat, double lon) {
        return graphInit.getNodeMap().values().stream()
                .min(Comparator.comparingDouble(
                        node -> HaversineUtil.haversine(lat, lon, node.lat(), node.lon())
                ))
                .map(Node::id)
                .orElseThrow(() -> new ApplicationException(WalkingErrorCase.NODE_NOT_FOUND));
    }
}
