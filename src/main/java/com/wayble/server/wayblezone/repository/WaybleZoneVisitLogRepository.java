package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WaybleZoneVisitLogRepository extends JpaRepository<WaybleZoneVisitLog, Long> {
    boolean existsByUserIdAndZoneId(Long userId, Long zoneId);

    Optional<WaybleZoneVisitLog> findByUserIdAndZoneId(Long userId, Long zoneId);
    
    @Modifying
    @Query("DELETE FROM WaybleZoneVisitLog v WHERE v.zoneId = :zoneId")
    void deleteByZoneId(@Param("zoneId") Long zoneId);
}
