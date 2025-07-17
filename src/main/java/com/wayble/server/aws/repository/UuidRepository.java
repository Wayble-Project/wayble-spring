package com.wayble.server.aws.repository;

import com.wayble.server.aws.domain.UuidEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UuidRepository extends JpaRepository<UuidEntity, Long> {
}
