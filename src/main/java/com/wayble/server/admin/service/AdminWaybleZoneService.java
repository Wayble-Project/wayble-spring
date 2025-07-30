package com.wayble.server.admin.service;

import com.wayble.server.admin.dto.AdminWaybleZoneDetailDto;
import com.wayble.server.admin.dto.AdminWaybleZoneThumbnailDto;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminWaybleZoneService {
    private final WaybleZoneRepository waybleZoneRepository;

    public long getTotalWaybleZoneCounts() {
        return waybleZoneRepository.count();
    }

    public List<AdminWaybleZoneThumbnailDto> findWaybleZonesByPage(int page, int size) {
        return waybleZoneRepository.findWaybleZonesWithPaging(page, size);
    }

    public AdminWaybleZoneDetailDto findWaybleZoneById(long waybleZoneId) {
        Optional<AdminWaybleZoneDetailDto> optionalWaybleZone = waybleZoneRepository.findAdminWaybleZoneDetailById(waybleZoneId);
        return optionalWaybleZone.orElse(null);
    }
}
