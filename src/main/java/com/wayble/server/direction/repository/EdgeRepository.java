package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.transportation.Edge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdgeRepository extends JpaRepository<Edge, Long> {
}
