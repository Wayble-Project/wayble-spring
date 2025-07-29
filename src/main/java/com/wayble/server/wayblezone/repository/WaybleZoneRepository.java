package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaybleZoneRepository extends JpaRepository<WaybleZone, Long>, WaybleZoneRepositoryCustom {
    List<WaybleZone> findByAddress_CityContainingAndZoneType(String city, com.wayble.server.wayblezone.entity.WaybleZoneType category);
}
