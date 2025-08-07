package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Facility;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByNodeId(Long nodeId);
}
