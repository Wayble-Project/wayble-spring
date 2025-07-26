package com.wayble.server.user.dto;

import com.wayble.server.user.entity.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequestDto(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password,

        @NotNull(message = "로그인 타입은 필수입니다")
        LoginType loginType
) {}