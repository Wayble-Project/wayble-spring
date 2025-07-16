package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.UserType;

import java.time.LocalDate;

public record UserRegisterRequestDto(
        String nickname,
        String username,
        String email,
        String password,
        LocalDate birthDate,
        Gender gender,
        LoginType loginType,
        UserType userType
) {}
