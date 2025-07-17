package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.UserType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserRegisterDto(
        Long userId,
        String nickname,
        String username,
        String email,
        String password,
        LocalDate birthDate,
        Gender gender,
        UserType userType
) {

}
