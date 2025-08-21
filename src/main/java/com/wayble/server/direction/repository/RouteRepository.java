package com.wayble.server.direction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wayble.server.direction.entity.transportation.Route;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long>{
    
    @Query("SELECT r.routeName FROM Route r WHERE r.routeId = :routeId")
    Optional<String> findRouteNameById(@Param("routeId") Long routeId);
}
