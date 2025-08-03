package com.wayble.server.direction.repository;

import com.wayble.server.direction.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
