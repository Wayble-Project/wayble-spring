package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, Long> {
    @Query("SELECT DISTINCT e FROM Edge e " +
           "JOIN FETCH e.startNode " +
           "JOIN FETCH e.endNode " +
           "LEFT JOIN FETCH e.route")
    List<Edge> findAllWithNodesAndRoute();
}
