package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, Long> {
    @Query("SELECT DISTINCT e FROM Edge e " +
           "JOIN FETCH e.startNode " +
           "JOIN FETCH e.endNode " +
           "LEFT JOIN FETCH e.route")
    List<Edge> findAllWithNodesAndRoute();
    
    @Query("SELECT e.id, e.startNode.id, e.endNode.id, e.edgeType, " +
           "s.stationName, s.latitude, s.longitude, " +
           "en.stationName, en.latitude, en.longitude, " +
           "r.routeId, r.routeName " +
           "FROM Edge e " +
           "JOIN e.startNode s " +
           "JOIN e.endNode en " +
           "LEFT JOIN e.route r " +
           "WHERE (s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLon AND :maxLon) OR " +
           "(en.latitude BETWEEN :minLat AND :maxLat AND en.longitude BETWEEN :minLon AND :maxLon)")
    List<Object[]> findEdgesInBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );
}
