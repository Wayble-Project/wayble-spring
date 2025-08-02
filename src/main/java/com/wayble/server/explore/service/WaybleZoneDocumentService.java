package com.wayble.server.explore.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.wayblezone.dto.WaybleZoneRegisterDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.exception.SearchErrorCase;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleZoneDocumentService {

    private final WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    public WaybleZoneDocument getWaybleZoneDocumentById(Long id) {
        return waybleZoneDocumentRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(SearchErrorCase.NO_SUCH_DOCUMENT));
    }

    public void saveDocumentFromEntity(WaybleZone waybleZone) {
        waybleZoneDocumentRepository.save(WaybleZoneDocument.fromEntity(waybleZone));
    }

    public void saveDocumentFromDto(WaybleZoneRegisterDto dto) {
        waybleZoneDocumentRepository.save(WaybleZoneDocument.fromDto(dto));
    }

    public void deleteDocumentById(Long id) {
        waybleZoneDocumentRepository.deleteById(id);
    }
}
