package com.wayble.server.explore.service;

import com.wayble.server.explore.dto.facility.WaybleFacilityConditionDto;
import com.wayble.server.explore.dto.facility.WaybleFacilityResponseDto;
import com.wayble.server.explore.repository.facility.WaybleFacilityQuerySearchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleFacilityDocumentService {

    private final WaybleFacilityQuerySearchRepository waybleFacilityQuerySearchRepository;

    public List<WaybleFacilityResponseDto> findNearbyFacilityDocuments(WaybleFacilityConditionDto dto) {
        return waybleFacilityQuerySearchRepository.findNearbyFacilitiesByType(dto);
    }
}
