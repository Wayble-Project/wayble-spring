package com.wayble.server.admin.repository;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<User, Long> {
    
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
    
    @Query(value = """
        SELECT u.id, u.nickname, u.email, u.birth_date, u.gender, 
               u.login_type, u.user_type, u.disability_type, u.mobility_aid
        FROM user u
        WHERE u.deleted_at IS NOT NULL
        ORDER BY u.deleted_at DESC
        LIMIT :size OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findDeletedUsersWithPaging(@Param("offset") int offset, @Param("size") int size);
    
    @Query(value = """
        SELECT u.id, u.nickname, u.username, u.email, u.birth_date, u.gender,
               u.login_type, u.user_type, u.profile_image_url, u.disability_type,
               u.mobility_aid, u.createdAt, u.updatedAt
        FROM user u
        WHERE u.id = :userId AND u.deleted_at IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> findDeletedUserDetailById(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM user WHERE deleted_at IS NOT NULL", nativeQuery = true)
    long countDeletedUsers();
}