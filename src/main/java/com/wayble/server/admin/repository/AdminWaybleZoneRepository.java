package com.wayble.server.admin.repository;

import com.wayble.server.wayblezone.entity.WaybleZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminWaybleZoneRepository extends JpaRepository<WaybleZone, Long>, AdminWaybleZoneRepositoryCustom {
}