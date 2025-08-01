package com.wayble.server.admin.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.UserType;

import java.time.LocalDate;

public record AdminUserThumbnailDto(
    Long id,
    String nickname,
    String email,
    LocalDate birthDate,
    Gender gender,
    LoginType loginType,
    UserType userType,
    String disabilityType,
    String mobilityAid
) {
}