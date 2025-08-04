package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.TransportationResponseDto;
import com.wayble.server.direction.dto.toilet.KricToiletRawItem;
import com.wayble.server.direction.dto.toilet.KricToiletRawResponse;
import com.wayble.server.direction.entity.transportation.Facility;
import com.wayble.server.direction.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final RestTemplate restTemplate;

    @Value("${kric.api.key}")
    private String kricApiKey;

    public TransportationResponseDto.NodeInfo getNodeInfo(Long NodeId){
        Facility facility = facilityRepository.findById(NodeId).orElse(null);
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
        String url = UriComponentsBuilder.fromHttpUrl("https://data.kric.go.kr/api/vulnerableUserInfo/stationDisabledToilet")
                .queryParam("serviceKey", kricApiKey)
                .queryParam("format", "json")
                .queryParam("railOprIsttCd", facility.getRailOprLsttCd())
                .queryParam("lnCd", facility.getLnCd())
                .queryParam("stinCd", facility.getStinCd())
                .toUriString();
        ResponseEntity<KricToiletRawResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<KricToiletRawResponse>() {}
        );

        List<KricToiletRawItem> items = response.getBody().body().item();

        // 역별로 화장실 존재 여부 추출 (중복 제거)
        Map<String, Boolean> stationToiletMap = new HashMap<>();
        for (KricToiletRawItem item : items) {
            String stinCd = item.stinCd();
            int toiletCount = 0;
            try {
                toiletCount = Integer.parseInt(item.toltNum());
            } catch (NumberFormatException ignored) {}
            stationToiletMap.put(stinCd, stationToiletMap.getOrDefault(stinCd, false) || toiletCount > 0);
        }

        return stationToiletMap;
    }
}
