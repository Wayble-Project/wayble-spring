package com.wayble.server.user.repository;

import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.wayblezone.entity.WaybleZone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserPlaceWaybleZoneMappingRepository extends JpaRepository<UserPlaceWaybleZoneMapping, Long> {
    boolean existsByUserPlace_User_IdAndWaybleZone_Id(Long userId, Long zoneId);

    @EntityGraph(attributePaths = {"userPlace", "waybleZone"})
    List<UserPlaceWaybleZoneMapping> findAllByUserPlace_User_Id(Long userId);

    // 리스트 내부 웨이블존만 바로 반환
    @Query("""
            select m.waybleZone
            from UserPlaceWaybleZoneMapping m
            where m.userPlace.id = :placeId
            """)
    List<WaybleZone> findZonesByPlaceId(Long placeId);

    long countByUserPlace_Id(Long placeId);

    void deleteByUserPlace_IdAndWaybleZone_Id(Long placeId, Long waybleZoneId);
}