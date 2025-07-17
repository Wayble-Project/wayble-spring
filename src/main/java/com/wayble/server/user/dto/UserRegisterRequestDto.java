package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.UserType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegisterRequestDto(
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 8, message = "닉네임은 8자 이하여야 합니다")
        String nickname,

        @NotBlank(message = "사용자명은 필수입니다")
        String username,

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String password,

        @NotNull(message = "생년월일은 필수입니다")
        @Past(message = "생년월일은 과거 날짜여야 합니다")
        LocalDate birthDate,

        @NotNull(message = "성별은 필수입니다")
        Gender gender,

        @NotNull(message = "로그인 타입은 필수입니다")
        LoginType loginType,

        @NotNull(message = "사용자 타입은 필수입니다")
        UserType userType
) {}