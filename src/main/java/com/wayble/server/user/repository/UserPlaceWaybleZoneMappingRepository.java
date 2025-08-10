package com.wayble.server.user.repository;

import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPlaceWaybleZoneMappingRepository extends JpaRepository<UserPlaceWaybleZoneMapping, Long> {
    boolean existsByUserPlace_User_IdAndWaybleZone_Id(Long userId, Long zoneId);

    @EntityGraph(attributePaths = {"userPlace", "waybleZone"})
    List<UserPlaceWaybleZoneMapping> findAllByUserPlace_User_Id(Long userId);
}