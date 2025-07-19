package com.wayble.server.user.repository;

import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndLoginType(String email, LoginType loginType);
    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);
    @Query(value = "SELECT * FROM user WHERE email = :email AND login_type = :#{#loginType.name()} AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<User> findDeletedUserByEmailAndLoginType(String email, @Param("loginType") LoginType loginType);
}
