package com.wayble.server.explore.service;

import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.explore.repository.search.WaybleZoneQuerySearchRepository;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.explore.dto.search.request.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneSearchConditionDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.exception.SearchErrorCase;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaybleZoneSearchService {

    private final WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    private final WaybleZoneQuerySearchRepository waybleZoneQuerySearchRepository;

    public WaybleZoneDocument getWaybleZoneDocumentById(Long id) {
        return waybleZoneDocumentRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(SearchErrorCase.NO_SUCH_DOCUMENT));
    }

    public void saveDocumentFromEntity(WaybleZone waybleZone) {
        waybleZoneDocumentRepository.save(WaybleZoneDocument.fromEntity(waybleZone));
    }

    public void saveDocumentFromDto(WaybleZoneDocumentRegisterDto dto) {
        waybleZoneDocumentRepository.save(WaybleZoneDocument.fromDto(dto));
    }

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
}
