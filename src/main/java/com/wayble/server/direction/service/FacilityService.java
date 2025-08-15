package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.transportation.Facility;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.entity.transportation.Wheelchair;
import com.wayble.server.direction.external.kric.dto.KricToiletRawItem;
import com.wayble.server.direction.external.kric.dto.KricToiletRawResponse;
import com.wayble.server.direction.repository.FacilityRepository;
import com.wayble.server.direction.repository.NodeRepository;
import com.wayble.server.direction.repository.WheelchairInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final FacilityRepository facilityRepository;
    private final NodeRepository nodeRepository;
    private final WheelchairInfoRepository wheelchairInfoRepository;
    private final WebClient kricWebClient;
    private final KricProperties kricProperties;

    public TransportationResponseDto.NodeInfo getNodeInfo(Long nodeId, Long routeId) {
        List<String> wheelchair = new ArrayList<>();
        List<String> elevator = new ArrayList<>();
        Boolean accessibleRestroom = false;

        Optional<Node> nodeOpt = nodeRepository.findById(nodeId);
        
        if (nodeOpt.isPresent()) {
            Node node = nodeOpt.get();
            
            if (routeId != null) {
                List<Wheelchair> wheelchairs = wheelchairInfoRepository.findByRouteId(routeId);
                for (Wheelchair wheelchairInfo : wheelchairs) {
                    String location = wheelchairInfo.getWheelchairLocation();
                    if (location != null && !location.trim().isEmpty()) {
                        wheelchair.add(location.trim());
                    }
                }
            }
            
            elevator = new ArrayList<>();
            
            Facility facility = facilityRepository.findByNodeId(nodeId).orElse(null);
            if (facility != null) {
                String stinCd = facility.getStinCd();
                String railOprLsttCd = facility.getRailOprLsttCd();
                String lnCd = facility.getLnCd();
                
                if (stinCd != null && railOprLsttCd != null && lnCd != null) {
                    Map<String, Boolean> toiletInfo = getToiletInfo(facility);
                    accessibleRestroom = toiletInfo.getOrDefault(stinCd, false);
                }
            }
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
        } catch(Exception e) {
            log.error("KRIC API 호출 실패: {}", e.getMessage());
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
}
