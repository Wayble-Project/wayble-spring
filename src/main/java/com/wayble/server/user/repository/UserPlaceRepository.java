package com.wayble.server.user.repository;

import com.wayble.server.user.entity.UserPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlaceRepository extends JpaRepository<UserPlace, Long> {
    Optional<UserPlace> findByUser_IdAndTitle(Long userId, String title);
    List<UserPlace> findAllByUser_IdOrderByCreatedAtDesc(Long userId); // 사용자 리스트 전체(최신순)
}
