package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Facility;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    @Query("SELECT f FROM Facility f " +
           "LEFT JOIN FETCH f.lifts " +
           "WHERE f.id = :nodeId")
    Optional<Facility> findByNodeId(@Param("nodeId") Long nodeId);
}
