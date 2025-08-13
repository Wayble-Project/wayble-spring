package com.wayble.server.user.repository;

import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.wayblezone.entity.WaybleZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPlaceWaybleZoneMappingRepository extends JpaRepository<UserPlaceWaybleZoneMapping, Long> {
    boolean existsByUserPlace_User_IdAndWaybleZone_Id(Long userId, Long zoneId);

    @EntityGraph(attributePaths = {"userPlace", "waybleZone"})
    List<UserPlaceWaybleZoneMapping> findAllByUserPlace_User_Id(Long userId);
    boolean existsByUserPlace_IdAndWaybleZone_Id(Long placeId, Long zoneId);
    void deleteByUserPlace_IdAndWaybleZone_Id(Long placeId, Long zoneId);

    // 리스트 내부 웨이블존 조회 (페이징 포함)
    @Query("""
           select m.waybleZone
           from UserPlaceWaybleZoneMapping m
           where m.userPlace.id = :placeId
           order by m.id desc
           """)
    Page<WaybleZone> findZonesByPlaceId(@Param("placeId") Long placeId, Pageable pageable);






}