package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZoneOperatingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface WaybleZoneOperatingHourRepository extends JpaRepository<WaybleZoneOperatingHour, Long> {
    List<WaybleZoneOperatingHour> findByWaybleZone_Id(Long waybleZoneId);
    void deleteByWaybleZone_Id(Long waybleZoneId);
    boolean existsByWaybleZone_IdAndDayOfWeek(Long waybleZoneId, DayOfWeek dayOfWeek);
}