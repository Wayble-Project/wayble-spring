package com.wayble.server.explore.service;

import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.repository.search.WaybleZoneQuerySearchRepository;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaybleZoneSearchService {

    private final WaybleZoneQuerySearchRepository waybleZoneQuerySearchRepository;

    private final WaybleZoneRepository waybleZoneRepository;

    public Slice<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(
            WaybleZoneSearchConditionDto condition,
            Pageable pageable)
    {
        return waybleZoneQuerySearchRepository.searchWaybleZonesByCondition(condition, pageable);
    }

    public List<WaybleZoneDistrictResponseDto> searchMostSearchesWaybleZoneByDistrict(String district) {
        if (district == null) {
            return null;
        }

        return waybleZoneQuerySearchRepository.findTop3SearchesWaybleZonesByDistrict(district);
    }

    public List<WaybleZoneDistrictResponseDto> searchMostLikesWaybleZoneByDistrict(String district) {
        if (district == null) {
            return null;
        }

        return waybleZoneRepository.findTop3likesWaybleZonesByDistrict(district);
    }
}
