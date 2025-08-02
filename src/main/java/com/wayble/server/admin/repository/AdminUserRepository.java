package com.wayble.server.admin.repository;

import com.wayble.server.admin.dto.user.AdminUserDetailDto;
import com.wayble.server.admin.dto.user.AdminUserThumbnailDto;
import com.wayble.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<User, Long> {
    
    @Query(value = """
        SELECT u.id, u.nickname, u.email, u.birth_date, u.gender, 
               u.login_type, u.user_type, u.disability_type, u.mobility_aid
        FROM user u
        WHERE u.deleted_at IS NULL
        ORDER BY u.created_at DESC
        LIMIT :size OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findUsersWithPagingRaw(@Param("offset") int offset, @Param("size") int size);
    
    @Query(value = """
        SELECT u.id, u.nickname, u.username, u.email, u.birth_date, u.gender, 
               u.login_type, u.user_type, u.profile_image_url, u.disability_type, 
               u.mobility_aid, u.created_at, u.updated_at
        FROM user u 
        WHERE u.id = :userId AND u.deleted_at IS NULL
        """, nativeQuery = true)
    List<Object[]> findAdminUserDetailByIdRaw(@Param("userId") Long userId);
    
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
               u.mobility_aid, NOW(), NOW()
        FROM user u
        WHERE u.id = :userId AND u.deleted_at IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> findDeletedUserDetailById(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM user WHERE deleted_at IS NOT NULL", nativeQuery = true)
    long countDeletedUsers();
    
    @Modifying
    @Query(value = """
        UPDATE user 
        SET deleted_at = NULL
        WHERE id = :userId AND deleted_at IS NOT NULL
        """, nativeQuery = true)
    int restoreUserById(@Param("userId") Long userId);
    
    @Modifying
    @Query(value = """
        UPDATE review 
        SET deleted_at = NULL
        WHERE user_id = :userId AND deleted_at IS NOT NULL
        """, nativeQuery = true)
    int restoreUserReviews(@Param("userId") Long userId);
    
    @Modifying
    @Query(value = """
        UPDATE review_image ri
        INNER JOIN review r ON ri.review_id = r.id
        SET ri.deleted_at = NULL
        WHERE r.user_id = :userId AND ri.deleted_at IS NOT NULL
        """, nativeQuery = true)
    int restoreUserReviewImages(@Param("userId") Long userId);
    
    @Modifying
    @Query(value = """
        UPDATE user_place 
        SET deleted_at = NULL
        WHERE user_id = :userId AND deleted_at IS NOT NULL
        """, nativeQuery = true)
    int restoreUserPlaces(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM review WHERE user_id = :userId AND deleted_at IS NULL", nativeQuery = true)
    long countUserReviews(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM user_place WHERE user_id = :userId AND deleted_at IS NULL", nativeQuery = true)
    long countUserPlaces(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM review WHERE user_id = :userId", nativeQuery = true)
    long countUserReviewsIncludingDeleted(@Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM user_place WHERE user_id = :userId", nativeQuery = true)
    long countUserPlacesIncludingDeleted(@Param("userId") Long userId);
}