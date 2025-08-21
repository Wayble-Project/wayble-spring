package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.transportation.Facility;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.entity.transportation.Wheelchair;
import com.wayble.server.direction.entity.transportation.Elevator;

import com.wayble.server.direction.external.kric.dto.KricToiletRawItem;
import com.wayble.server.direction.external.kric.dto.KricToiletRawResponse;

import com.wayble.server.direction.repository.ElevatorRepository;
import com.wayble.server.direction.repository.FacilityRepository;
import com.wayble.server.direction.repository.NodeRepository;
import com.wayble.server.direction.repository.RouteRepository;
import com.wayble.server.direction.repository.WheelchairInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Builder;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.wayble.server.direction.external.kric.KricProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityService {
    private final ElevatorRepository elevatorRepository;
    private final FacilityRepository facilityRepository;
    private final NodeRepository nodeRepository;
    private final WheelchairInfoRepository wheelchairInfoRepository;
    private final WebClient kricWebClient;
    private final KricProperties kricProperties;

    public TransportationResponseDto.NodeInfo getNodeInfo(Long nodeId, Long routeId) {
        List<String> wheelchair = new ArrayList<>();
        List<String> elevator = new ArrayList<>();
        Boolean accessibleRestroom = false;

        
        if (routeId != null) {
            List<String> wheelchairLocations = wheelchairInfoRepository.findWheelchairLocationsByRouteId(routeId);
            for (String location : wheelchairLocations) {
                if (location != null && !location.trim().isEmpty()) {
                    wheelchair.add(location.trim());
                }
            }
        }
        
        Optional<Object[]> facilityData = facilityRepository.findByNodeId(nodeId);
        if (facilityData.isPresent()) {
            Object[] data = facilityData.get();
            String stinCd = (String) data[3]; // stinCd
            String railOprLsttCd = (String) data[4]; // railOprLsttCd
            String lnCd = (String) data[2]; // lnCd
                
            if (stinCd != null && railOprLsttCd != null && lnCd != null) {
                // Facility 객체 생성
                Facility facility = Facility.builder()
                    .id((Long) data[0])
                    .stationName((String) data[1])
                    .lnCd(lnCd)
                    .railOprLsttCd(railOprLsttCd)
                    .stinCd(stinCd)
                    .build();
                    
                Map<String, Boolean> toiletInfo = getToiletInfo(facility);
                accessibleRestroom = toiletInfo.getOrDefault(stinCd, false);
                
                elevator = getElevatorInfo(facility, routeId);
            } else {
                log.error("Facility 정보 누락 - nodeId: {}, stinCd: {}, railOprLsttCd: {}, lnCd: {}", 
                    nodeId, stinCd, railOprLsttCd, lnCd);
            }
        } else {
            log.error("Facility 정보 없음 - nodeId: {}", nodeId);
        }

        return new TransportationResponseDto.NodeInfo(
                wheelchair,
                elevator,
                accessibleRestroom
        );
    }



    private Map<String, Boolean> getToiletInfo(Facility facility) {
        String uri = UriComponentsBuilder.fromPath("/openapi/vulnerableUserInfo/stationDisabledToilet")
                .queryParam("serviceKey", kricProperties.key())
                .queryParam("format", "json")
                .queryParam("railOprIsttCd", facility.getRailOprLsttCd())
                .queryParam("lnCd", facility.getLnCd())
                .queryParam("stinCd", facility.getStinCd())
                .toUriString();
        
        List<KricToiletRawItem> items;
        try{
            KricToiletRawResponse response = kricWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(KricToiletRawResponse.class)
                    .block();
            
            if (response == null || response.body() == null) {
                return new HashMap<>();
            }
            
            items = response.body();
            if (items == null) {
                return new HashMap<>();
            }
        } catch(Exception e) {
            log.error("KRIC API 호출 실패 - stinCd: {}, railOprIsttCd: {}, lnCd: {}, error: {}", 
                facility.getStinCd(), facility.getRailOprLsttCd(), facility.getLnCd(), e.getMessage(), e);
            return new HashMap<>();
        }

        Map<String, Boolean> stationToiletMap = new HashMap<>();
        if (items != null) {
            for (KricToiletRawItem item : items) {
                String stinCd = item.stinCd();
                int toiletCount = 0;
                try {
                    toiletCount = Integer.parseInt(item.toltNum());
                } catch (NumberFormatException e) {
                    log.warn("장애인 화장실 정보 파싱 실패. stinCd: {}, toltNum: {}", stinCd, item.toltNum());
                }
                boolean hasToilet = stationToiletMap.getOrDefault(stinCd, false) || toiletCount > 0;
                stationToiletMap.put(stinCd, hasToilet);
            }
        }

        return stationToiletMap;
    }

    private List<String> getElevatorInfo(Facility facility, Long routeId) {
        List<String> elevatorLocations = new ArrayList<>();
        
        try {
            List<Elevator> elevators = elevatorRepository.findByFacility(facility);
        
            for (Elevator elevator : elevators) {
                String location = elevator.getLocation();
                if (location != null && !location.trim().isEmpty()) {
                    elevatorLocations.add(location.trim());
                }
            }
            
            elevatorLocations.sort(String::compareTo);
            
        } catch(Exception e) {
            log.error("엘리베이터 정보 조회 실패 - facilityId: {}, error: {}", 
                facility.getId(), e.getMessage(), e);
        }
        
        return elevatorLocations;
    }


}