package com.wayble.server.admin.dto.user;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserDetailDto(
    Long id,
    String nickname,
    String username,
    String email,
    LocalDate birthDate,
    Gender gender,
    LoginType loginType,
    UserType userType,
    String profileImageUrl,
    String disabilityType,
    String mobilityAid,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}