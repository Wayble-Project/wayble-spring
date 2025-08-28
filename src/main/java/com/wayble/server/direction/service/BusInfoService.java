package com.wayble.server.direction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.direction.external.opendata.OpenDataProperties;
import com.wayble.server.direction.external.opendata.dto.OpenDataResponse;
import com.wayble.server.direction.external.opendata.dto.StationSearchResponse;
import com.wayble.server.direction.repository.transportation.RouteRepository;
import com.wayble.server.direction.dto.response.TransportationResponseDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusInfoService {

    private final WebClient openDataWebClient;
    private final OpenDataProperties openDataProperties;
    private final RouteRepository routeRepository;

    public TransportationResponseDto.BusInfo getBusInfo(String stationName, Long busId, Double x, Double y) {
        
        List<Boolean> isLowFloor = new ArrayList<>();
        Integer dispatchInterval = null;

        // 나중에 서비스키 문제 해결되면 이 함수 호출 제거
        return createDummyBusInfo(stationName, busId, x, y);

        
        /*
        boolean isShuttleBus = false;
        if (busId != null) {
            var routeName = routeRepository.findRouteNameById(busId);
            isShuttleBus = routeName.isPresent() && routeName.get().contains("마포");
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

            // 2. 정류소가 1개면 바로 선택, 여러 개면 가장 가까운 정류소 찾기
            List<StationSearchResponse.StationItem> stationList = stationSearchResponse.msgBody().itemList();
            StationSearchResponse.StationItem closestStation;
            
            if (stationList.size() == 1) {
                // 정류소가 1개뿐이면 불필요한 거리 계산 및 API 호출 없이 바로 선택
                closestStation = stationList.get(0);
            } else {
                // 여러 정류소가 있을 때만 findClosestStation 호출
                closestStation = findClosestStation(stationList, x, y, busId);
            }

            if (closestStation == null) {
                log.warn("가장 가까운 정류소를 찾을 수 없습니다: {}", stationName);
                return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
            }
            
            // 3. 정류소 ID로 버스 도착 정보 조회
            OpenDataResponse openDataResponse = fetchArrivals(closestStation.arsId(), busId);
            if (openDataResponse == null || openDataResponse.msgBody() == null || 
                openDataResponse.msgBody().itemList() == null) {
                log.warn("정류소 {}의 버스 도착 정보를 찾을 수 없습니다: {}", stationName, closestStation.arsId());
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
                    log.warn("⚠️ 배차간격 파싱 실패: {}", item.term());
                }
                
                count++;
            }
        
        } catch (Exception e) {
            log.error("버스 정보 조회 중 오류 발생: {}", e.getMessage());
            return new TransportationResponseDto.BusInfo(isShuttleBus, new ArrayList<>(), null);
        }

        return new TransportationResponseDto.BusInfo(isShuttleBus, isLowFloor, dispatchInterval);
               */
        
    }

    // 나중에 이 함수 제거
    private TransportationResponseDto.BusInfo createDummyBusInfo(String stationName, Long busId, Double x, Double y) {
        
        // 셔틀버스 여부 확인 (기존 로직 유지)
        boolean isShuttleBus = false;
        if (busId != null) {
            var route = routeRepository.findById(busId);
            isShuttleBus = route.isPresent() && route.get().getRouteName().contains("마포");
        }
        
        // 랜덤 더미 데이터 생성
        List<Boolean> isLowFloor = new ArrayList<>();
        isLowFloor.add(Math.random() < 0.7);
        isLowFloor.add(Math.random() < 0.5);
        
        Integer dispatchInterval = (int) (Math.random() * 15) + 1;
        
        
        return new TransportationResponseDto.BusInfo(isShuttleBus, isLowFloor, dispatchInterval);
    }

    private OpenDataResponse fetchArrivals(String stationId, Long busId) {
        try {
            String serviceKey = openDataProperties.encodedKey();

            String fullUri = openDataProperties.baseUrl() + 
                    openDataProperties.endpoints().arrivals() +
                    "?serviceKey=" + serviceKey +
                    "&arsId=" + stationId +
                    "&resultType=json";
            

            // 먼저 raw 응답을 확인
            String rawResponse = openDataWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(openDataProperties.endpoints().arrivals())
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("arsId", stationId)
                            .queryParam("resultType", "json")
                            .build())
                    .header("Accept", openDataProperties.accept())
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("공공데이터 API 호출 오류: {}", body);
                                    return Mono.error(new RuntimeException("API 호출 실패: " + response.statusCode()));
                                });
                    })
                    .bodyToMono(String.class)
                    .block();
                       
            OpenDataResponse originalResponse = null;
            if (rawResponse != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    originalResponse = objectMapper.readValue(rawResponse, OpenDataResponse.class);
                } catch (Exception e) {
                    log.error("JSON 파싱 실패: {}", e.getMessage());
                }
            }
            
            // busId가 맞는 버스만 필터링
            if (busId != null && originalResponse != null && originalResponse.msgBody() != null && 
                originalResponse.msgBody().itemList() != null) {
                
                // busId로 route 정보 조회
                var routeName = routeRepository.findRouteNameById(busId);
                String projectRouteName = routeName.orElse(null);
                
                List<OpenDataResponse.Item> filteredItems = originalResponse.msgBody().itemList().stream()
                    .filter(item -> {
                        if (projectRouteName == null || item.rtNm() == null) {
                            return false;
                        }
                        return projectRouteName.contains(item.rtNm()) || item.rtNm().contains(projectRouteName);
                    })
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

            String fullUri = openDataProperties.baseUrl() + 
                    openDataProperties.endpoints().stationByName() +
                    "?serviceKey=" + serviceKey +
                    "&stSrch=" + stationName +
                    "&resultType=json";



            String rawResponse = openDataWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(openDataProperties.endpoints().stationByName())
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("stSrch", stationName)
                            .queryParam("resultType", "json")
                            .build())
                    .header("Accept", openDataProperties.accept())
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("공공데이터 API 호출 오류: {}", body);
                                    return Mono.error(new RuntimeException("API 호출 실패: " + response.statusCode()));
                                });
                    })
                    .bodyToMono(String.class)
                    .block();
            
            StationSearchResponse stationResponse = null;
            if (rawResponse != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    stationResponse = objectMapper.readValue(rawResponse, StationSearchResponse.class);
                } catch (Exception e) {
                    log.error("JSON 파싱 실패: {}", e.getMessage());
                }
            }
            
            return stationResponse;

        } catch (Exception e) {
            log.error("정류소 검색 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    private StationSearchResponse.StationItem findClosestStation(List<StationSearchResponse.StationItem> stations, Double x, Double y, Long routeId) {
        if (stations == null || stations.isEmpty()) {
            log.warn("❌ 정류소 목록이 비어있음");
            return null;
        }

        // 1. 먼저 모든 정류소를 거리순으로 정렬
        List<StationSearchResponse.StationItem> sortedStations = new ArrayList<>();
        for (StationSearchResponse.StationItem station : stations) {
            try {
                String tmXStr = station.tmX();
                String tmYStr = station.tmY();
                
                if (tmXStr == null || tmYStr == null || tmXStr.trim().isEmpty() || tmYStr.trim().isEmpty() ||
                    "null".equalsIgnoreCase(tmXStr.trim()) || "null".equalsIgnoreCase(tmYStr.trim())) {
                    continue;
                }
                
                double stationX = Double.parseDouble(tmXStr);
                double stationY = Double.parseDouble(tmYStr);
                double distance = Math.sqrt(Math.pow(stationX - x, 2) + Math.pow(stationY - y, 2));
                
                // 거리 정보를 포함한 Wrapper 클래스 대신 정렬용 로직 사용
                sortedStations.add(station);
            } catch (NumberFormatException e) {
                log.warn("정류소 좌표 파싱 실패 - {}: tmX={}, tmY={}", station.stNm(), station.tmX(), station.tmY());
                continue;
            }
        }
        
        // 거리순으로 정렬
        sortedStations.sort((s1, s2) -> {
            try {
                double d1 = Math.sqrt(Math.pow(Double.parseDouble(s1.tmX()) - x, 2) + Math.pow(Double.parseDouble(s1.tmY()) - y, 2));
                double d2 = Math.sqrt(Math.pow(Double.parseDouble(s2.tmX()) - x, 2) + Math.pow(Double.parseDouble(s2.tmY()) - y, 2));
                return Double.compare(d1, d2);
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        // 2. routeId가 있으면 가장 가까운 상위 3개 정류소에서만 해당 버스가 운행되는지 확인
        if (routeId != null) {
            var route = routeRepository.findById(routeId);
            String projectRouteName = route.isPresent() ? route.get().getRouteName() : null;
            
            if (projectRouteName != null) {
                int maxStationsToCheck = Math.min(3, sortedStations.size()); // 최대 3개만 확인
                for (int i = 0; i < maxStationsToCheck; i++) {
                    StationSearchResponse.StationItem station = sortedStations.get(i);
                    try {
                        // 각 정류소에서 버스 정보 조회
                        OpenDataResponse busResponse = fetchArrivals(station.arsId(), null);
                        if (busResponse != null && busResponse.msgBody() != null && busResponse.msgBody().itemList() != null) {
                            for (OpenDataResponse.Item item : busResponse.msgBody().itemList()) {
                                if (item.rtNm() != null && 
                                    (projectRouteName.contains(item.rtNm()) || item.rtNm().contains(projectRouteName))) {

                                    return station;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("정류소 {}에서 버스 정보 조회 실패: {}", station.stNm(), e.getMessage());
                        continue;
                    }
                }
            }
        }

        // 해당 버스가 운행되는 정류소가 없으면 가장 가까운 정류소 선택
        return sortedStations.isEmpty() ? null : sortedStations.get(0);
    }
}
