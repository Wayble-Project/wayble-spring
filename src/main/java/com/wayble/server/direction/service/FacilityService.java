package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.TransportationResponseDto;
import com.wayble.server.direction.entity.Facility;
import com.wayble.server.direction.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;

    public TransportationResponseDto.NodeInfo getNodeInfo(Long NodeId){
        Facility facility = facilityRepository.findById(NodeId).orElse(null);
        List<TransportationResponseDto.LocationInfo> wheelchair = new ArrayList<>();
        List<TransportationResponseDto.LocationInfo> elevator = new ArrayList<>();
        Boolean accessibleRestroom = false; // 공공 api의 서비스키를 발급받은 뒤 수정 예정

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
        }

        return new TransportationResponseDto.NodeInfo(
                wheelchair,
                elevator,
                accessibleRestroom
        );
    }
}
