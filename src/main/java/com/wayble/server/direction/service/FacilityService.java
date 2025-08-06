package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.TransportationResponseDto;
import com.wayble.server.direction.entity.transportation.Facility;
import com.wayble.server.direction.external.kric.dto.KricToiletRawItem;
import com.wayble.server.direction.external.kric.dto.KricToiletRawResponse;
import com.wayble.server.direction.repository.FacilityRepository;
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
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final WebClient kricWebClient;
    private final KricProperties kricProperties;

    public TransportationResponseDto.NodeInfo getNodeInfo(Long nodeId){
        Facility facility = facilityRepository.findByNodeId(nodeId).orElse(null);
        List<TransportationResponseDto.LocationInfo> wheelchair = new ArrayList<>();
        List<TransportationResponseDto.LocationInfo> elevator = new ArrayList<>();
        Boolean accessibleRestroom = false;

        if (facility != null) {
            if (facility.getLifts() != null) {
                wheelchair = facility.getLifts().stream()
                        .map(lift -> new TransportationResponseDto.LocationInfo(
                                lift.getLatitude(),
                                lift.getLongitude()
                        ))
                        .toList();
            }

            if (facility.getElevators() != null) {
                elevator = facility.getElevators().stream()
                        .map(elev -> new TransportationResponseDto.LocationInfo(
                                elev.getLatitude(),
                                elev.getLongitude()
                        ))
                        .toList();
            }

            // Get toilet information
            Map<String, Boolean> toiletInfo = getToiletInfo(facility);
            String stinCd = facility.getStinCd();
            accessibleRestroom = toiletInfo.getOrDefault(stinCd, false);
        }

        return new TransportationResponseDto.NodeInfo(
                wheelchair,
                elevator,
                accessibleRestroom
        );
    }

    private Map<String, Boolean> getToiletInfo(Facility facility){
        String uri = UriComponentsBuilder.fromPath("/api/vulnerableUserInfo/stationDisabledToilet")
                .queryParam("serviceKey", kricProperties.key())
                .queryParam("format", "json")
                .queryParam("railOprLsttCd", facility.getRailOprLsttCd())
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

            items = response.body().item();

        } catch(Exception e){
            log.info("역사 화장실 api 호출 중 에러 발생: {}: {}", uri, e.getCause());
            return new HashMap<>();
        }

        // 역별로 화장실 존재 여부 추출 (중복 제거)
        Map<String, Boolean> stationToiletMap = new HashMap<>();
        for (KricToiletRawItem item : items) {
            String stinCd = item.stinCd();
            int toiletCount = 0;
            try {
                toiletCount = Integer.parseInt(item.toltNum());
            } catch (NumberFormatException e) {
                log.debug("지하철 역에 대해 잘못된 숫자 형식. 지하철역 번호 {}: {}", stinCd, item.toltNum());
            }
            stationToiletMap.put(stinCd, stationToiletMap.getOrDefault(stinCd, false) || toiletCount > 0);
        }

        return stationToiletMap;
    }
}
