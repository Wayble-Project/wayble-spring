package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaybleZoneVisitLogRepository extends JpaRepository<WaybleZoneVisitLog, Long> {
    boolean existsByUserIdAndZoneId(Long userId, Long zoneId);

    Optional<WaybleZoneVisitLog> findByUserIdAndZoneId(Long userId, Long zoneId);
}
