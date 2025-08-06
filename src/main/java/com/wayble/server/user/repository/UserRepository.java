package com.wayble.server.user.repository;

import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndLoginType(String email, LoginType loginType);
    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);
    boolean existsByNickname(String nickname);
}
