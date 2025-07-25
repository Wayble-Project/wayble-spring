package com.wayble.server.wayblezone.service;

import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import com.wayble.server.wayblezone.exception.WaybleZoneErrorCase;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import com.wayble.server.wayblezone.repository.WaybleZoneVisitLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class WaybleZoneVisitLogService {

    private final UserRepository userRepository;

    private final WaybleZoneRepository waybleZoneRepository;

    private final WaybleZoneVisitLogRepository waybleZoneVisitLogRepository;

    public void saveVisitLog(Long userId, Long zoneId) {

        if(userId == null || zoneId == null) {
            return;
        }

        Optional<WaybleZoneVisitLog> existingLog = waybleZoneVisitLogRepository
                               .findByUserIdAndZoneId(userId, zoneId);

        if(existingLog.isPresent()) {
            existingLog.get().updateVisitedAtToNow();
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        WaybleZone zone = waybleZoneRepository.findById(zoneId)
                .orElseThrow(() -> new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_NOT_FOUND));

        WaybleZoneVisitLog waybleZoneVisitLog = WaybleZoneVisitLog.builder()
                .userId(userId)
                .zoneId(zoneId)
                .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                .gender(user.getGender())
                .build();

        waybleZoneVisitLogRepository.save(waybleZoneVisitLog);
    }
}
