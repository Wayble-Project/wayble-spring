package com.wayble.server.common.user.repository;

import com.wayble.server.common.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
