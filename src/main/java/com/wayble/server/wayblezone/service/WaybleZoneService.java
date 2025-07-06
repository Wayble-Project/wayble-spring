package com.wayble.server.wayblezone.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.wayblezone.exception.WaybleZoneErrorCase;
import com.wayble.server.wayblezone.repository.WaybleZoneImageRepository;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaybleZoneService {

    private final WaybleZoneRepository waybleZoneRepository;

    private final WaybleZoneImageRepository imageRepository;

    public void makeException() {
        throw new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_NOT_FOUND);
    }
}
