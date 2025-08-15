package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Wheelchair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WheelchairInfoRepository extends JpaRepository<Wheelchair, Long> {
    
    @Query("SELECT w FROM Wheelchair w WHERE w.route.routeId = :routeId")
    List<Wheelchair> findByRouteId(@Param("routeId") Long routeId);
}
