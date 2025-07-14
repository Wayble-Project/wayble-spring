package com.wayble.server.explore.service;

import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.explore.entity.WaybleZoneVisitLogDocument;
import com.wayble.server.explore.exception.VisitLogErrorCase;
import com.wayble.server.explore.repository.WaybleZoneVisitLogDocumentRepository;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WaybleZoneVisitLogService {

    private final WaybleZoneVisitLogDocumentRepository visitLogDocumentRepository;

    private final UserRepository userRepository;

    public void saveVisitLog(Long userId, Long zoneId) {
        if(userId == null || zoneId == null) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(VisitLogErrorCase.USER_NOT_EXIST));

        WaybleZoneVisitLogDocument visitLogDocument = WaybleZoneVisitLogDocument.fromEntity(user, zoneId);
        visitLogDocumentRepository.save(visitLogDocument);
    }
}
