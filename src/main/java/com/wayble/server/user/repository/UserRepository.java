package com.wayble.server.user.repository;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndLoginType(String email, LoginType loginType);
    Optional<User> findByEmailAndLoginType(String email, LoginType loginType);
    
    @Query("""
        SELECT new com.wayble.server.admin.dto.user.AdminUserThumbnailDto(
            u.id, u.nickname, u.email, u.birthDate, u.gender, 
            u.loginType, u.userType, u.disabilityType, u.mobilityAid
        )
        FROM User u
        ORDER BY u.createdAt DESC
        LIMIT :size OFFSET :offset
        """)
    List<AdminUserThumbnailDto> findUsersWithPaging(@Param("offset") int offset, @Param("size") int size);
    
    @Query("""
        SELECT new com.wayble.server.admin.dto.user.AdminUserDetailDto(
            u.id, u.nickname, u.username, u.email, u.birthDate, u.gender, 
            u.loginType, u.userType, u.profileImageUrl, u.disabilityType, 
            u.mobilityAid, u.createdAt, u.updatedAt
        )
        FROM User u 
        WHERE u.id = :userId
        """)
    Optional<AdminUserDetailDto> findAdminUserDetailById(@Param("userId") Long userId);
}
