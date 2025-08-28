package com.wayble.server.direction.repository.transportation;

import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.repository.transportation.NodeBoundingBoxProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NodeRepository extends JpaRepository<Node, Long> {
    
    @Query("SELECT " +
           "n.id          as id, " +
           "n.stationName as stationName, " +
           "n.nodeType    as nodeType, " +
           "n.latitude    as latitude, " +
           "n.longitude   as longitude " +
           "FROM Node n WHERE " +
           "n.latitude BETWEEN :minLat AND :maxLat AND " +
           "n.longitude BETWEEN :minLon AND :maxLon " +
           "ORDER BY n.latitude, n.longitude")
    List<NodeBoundingBoxProjection> findNodesInBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );
}
