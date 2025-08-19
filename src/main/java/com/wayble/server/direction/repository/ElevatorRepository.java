package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Elevator;
import com.wayble.server.direction.entity.transportation.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ElevatorRepository extends JpaRepository<Elevator, Long> {
    @Query("SELECT e FROM Elevator e WHERE e.facility = :facility")
    List<Elevator> findByFacility(@Param("facility") Facility facility);
}

