package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaybleZoneFacilityRepository extends JpaRepository<WaybleZoneFacility, Long> {
    Optional<WaybleZoneFacility> findByWaybleZone_Id(Long waybleZoneId);
    void deleteByWaybleZone_Id(Long waybleZoneId);
}
