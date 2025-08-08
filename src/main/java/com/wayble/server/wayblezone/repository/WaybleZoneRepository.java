package com.wayble.server.wayblezone.repository;

import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WaybleZoneRepository extends JpaRepository<WaybleZone, Long>, WaybleZoneRepositoryCustom {

    // 목록 조회용: 시설 + 이미지 (운영시간 X)
    @EntityGraph(attributePaths = {"facility", "waybleZoneImageList"})
    @Query("""
           select distinct z
           from WaybleZone z
           where z.address.city like concat('%', :city, '%')
             and z.zoneType = :category
           """)
    List<WaybleZone> findSummaryByCityAndType(String city, WaybleZoneType category);

    // 상세 조회용: 시설 + 이미지 + 운영시간
    @EntityGraph(attributePaths = {"facility", "waybleZoneImageList"})
    @Query("""
           select distinct z
           from WaybleZone z
           where z.id = :id
           """)
    Optional<WaybleZone> findDetailById(Long id);

    long count();
}