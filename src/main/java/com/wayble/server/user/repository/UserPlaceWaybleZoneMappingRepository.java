package com.wayble.server.user.repository;

import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPlaceWaybleZoneMappingRepository extends JpaRepository<UserPlaceWaybleZoneMapping, Long> {
    boolean existsByUserPlace_User_IdAndWaybleZone_Id(Long userId, Long zoneId);
}
