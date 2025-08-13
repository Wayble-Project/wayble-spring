package com.wayble.server.direction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayble.server.direction.entity.transportation.Route;

public interface RouteRepository extends JpaRepository<Route, Long>{
    
}
