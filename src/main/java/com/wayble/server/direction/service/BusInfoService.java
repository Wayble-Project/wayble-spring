package com.wayble.server.direction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.direction.external.opendata.OpenDataProperties;
import com.wayble.server.direction.external.opendata.dto.OpenDataResponse;
import com.wayble.server.direction.external.opendata.dto.StationSearchResponse;
import com.wayble.server.direction.repository.RouteRepository;
import com.wayble.server.direction.dto.response.TransportationResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusInfoService {

    private final HttpClient httpClient;
    private final OpenDataProperties openDataProperties;
    private final RouteRepository routeRepository;

    public TransportationResponseDto.BusInfo getBusInfo(String stationName, Long busId, Double x, Double y) {
        List<Boolean> isLowFloor = new ArrayList<>();
        Integer dispatchInterval = null;

        boolean isShuttleBus = false;
        if (busId != null) {
            var route = routeRepository.findById(busId);
            isShuttleBus = route.isPresent() && route.get().getRouteName().contains("마포");
        }

        try {
            // 1. 정류소명으로 정류소 검색
            StationSearchResponse stationSearchResponse = fetchStationByName(stationName);
            if (stationSearchResponse == null || stationSearchResponse.msgBody() == null || 
                stationSearchResponse.msgBody().itemList() == null || 
                stationSearchResponse.msgBody().itemList().isEmpty()) {
                log.warn("정류소를 찾을 수 없습니다: {}", stationName);
                return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
            }

            // 2. 여러 정류소가 나올 때, 가장 가까운 정류소 찾기
            StationSearchResponse.StationItem closestStation = findClosestStation(
                    stationSearchResponse.msgBody().itemList(), x, y);

            if (closestStation == null) {
                log.warn("가장 가까운 정류소를 찾을 수 없습니다: {}", stationName);
                return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
            }

            // 3. 정류소 ID로 버스 도착 정보 조회
            OpenDataResponse openDataResponse = fetchArrivals(Long.parseLong(closestStation.stId()), busId);
            if (openDataResponse == null || openDataResponse.msgBody() == null || 
                openDataResponse.msgBody().itemList() == null) {
                log.warn("버스 도착 정보를 찾을 수 없습니다: {}", closestStation.stId());
                return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
            }
            
            // 4. 버스 정보 추출
            int count = 0;
            for (OpenDataResponse.Item item : openDataResponse.msgBody().itemList()) {
                if (count >= 1) break; // busId가 null일 때는 최대 1개 노선만
                
                // busType1과 busType2 추가
                isLowFloor.add("1".equals(item.busType1()));
                isLowFloor.add("1".equals(item.busType2()));
                
                // term을 정수로 변환
                try {
                    dispatchInterval = Integer.parseInt(item.term());
                } catch (NumberFormatException e) {
                    dispatchInterval = 0;
                }
                
                count++;
            }

        } catch (Exception e) {
            log.error("버스 정보 조회 중 오류 발생: {}", e.getMessage());
            return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
        }

        return new TransportationResponseDto.BusInfo(isShuttleBus, isLowFloor, dispatchInterval);
    }

    private OpenDataResponse fetchArrivals(Long stationId, Long busId) {
        try {
            String serviceKey = openDataProperties.encodedKey();

            String uri = openDataProperties.baseUrl() + 
                    openDataProperties.endpoints().arrivals() +
                    "?serviceKey=" + serviceKey +
                    "&stId=" + stationId +
                    "&resultType=json";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", openDataProperties.accept())
                    .GET()
                    .timeout(Duration.ofSeconds(openDataProperties.timeout()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            OpenDataResponse originalResponse = new ObjectMapper().readValue(response.body(), OpenDataResponse.class);
            
            // busId가 맞는 버스만 필터링
            if (busId != null && originalResponse != null && originalResponse.msgBody() != null && 
                originalResponse.msgBody().itemList() != null) {
                
                List<OpenDataResponse.Item> filteredItems = originalResponse.msgBody().itemList().stream()
                    .filter(item -> busId.toString().equals(item.busRouteId()))
                    .collect(Collectors.toList());
                
                return new OpenDataResponse(
                    originalResponse.comMsgHeader(),
                    originalResponse.msgHeader(),
                    new OpenDataResponse.MsgBody(filteredItems)
                );
            }
            
            return originalResponse;

        } catch (Exception e) {
            log.error("버스 도착 정보 조회 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    private StationSearchResponse fetchStationByName(String stationName) {
        try {
            String serviceKey = openDataProperties.encodedKey();

            String uri = openDataProperties.baseUrl() + 
                    openDataProperties.endpoints().stationByName() +
                    "?serviceKey=" + serviceKey +
                    "&stSrch=" + stationName +
                    "&resultType=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", openDataProperties.accept())
                    .GET()
                    .timeout(Duration.ofSeconds(openDataProperties.timeout()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new ObjectMapper().readValue(response.body(), StationSearchResponse.class);

        } catch (Exception e) {
            log.error("정류소 검색 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    private StationSearchResponse.StationItem findClosestStation(List<StationSearchResponse.StationItem> stations, Double x, Double y) {
        if (stations == null || stations.isEmpty()) {
            return null;
        }

        StationSearchResponse.StationItem closestStation = null;
        double minDistance = Double.MAX_VALUE;

        for (StationSearchResponse.StationItem station : stations) {
            try {
                // tmX, tmY가 숫자인지 확인하고 파싱
                String tmXStr = station.tmX();
                String tmYStr = station.tmY();
                
                if (tmXStr == null || tmYStr == null || tmXStr.trim().isEmpty() || tmYStr.trim().isEmpty()) {
                    log.warn("정류소 좌표가 null이거나 비어있음: {}", station.stNm());
                    continue;
                }
                
                double stationX = Double.parseDouble(tmXStr);
                double stationY = Double.parseDouble(tmYStr);
                
                double distance = Math.sqrt(Math.pow(stationX - x, 2) + Math.pow(stationY - y, 2));
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestStation = station;
                }
            } catch (NumberFormatException e) {
                log.warn("정류소 좌표 파싱 실패 - {}: tmX={}, tmY={}, error={}", 
                        station.stNm(), station.tmX(), station.tmY(), e.getMessage());
                continue;
            }
        }

        return closestStation;
    }
}
