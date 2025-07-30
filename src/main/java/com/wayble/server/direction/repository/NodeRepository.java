package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
}
