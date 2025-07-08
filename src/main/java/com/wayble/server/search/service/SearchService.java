package com.wayble.server.search.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.search.dto.WaybleZoneDocumentRegisterDto;
import com.wayble.server.search.dto.WaybleZoneSearchConditionDto;
import com.wayble.server.search.dto.WaybleZoneSearchResponseDto;
import com.wayble.server.search.entity.WaybleZoneDocument;
import com.wayble.server.search.exception.SearchErrorCase;
import com.wayble.server.search.repository.WaybleZoneSearchRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final WaybleZoneSearchRepository waybleZoneSearchRepository;

    public void makeException() {
        throw new ApplicationException(SearchErrorCase.SEARCH_EXCEPTION);
    }

    public WaybleZoneDocument getWaybleZoneDocumentById(Long id) {
        return waybleZoneSearchRepository.findById(id).orElse(null);
    }

    public void saveDocumentFromEntity(WaybleZone waybleZone) {
        waybleZoneSearchRepository.save(WaybleZoneDocument.fromEntity(waybleZone));
    }

    public void saveDocumentFromDto(WaybleZoneDocumentRegisterDto dto) {
        waybleZoneSearchRepository.save(WaybleZoneDocument.fromDto(dto));
    }

    public Slice<WaybleZoneSearchResponseDto> searchWaybleZonesByCondition(
            WaybleZoneSearchConditionDto condition,
            Pageable pageable)
    {
        return waybleZoneSearchRepository.searchWaybleZonesByCondition(condition, pageable);
    }
}
