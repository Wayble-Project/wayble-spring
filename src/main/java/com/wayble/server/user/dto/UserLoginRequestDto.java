package com.wayble.server.user.dto;

public record UserLoginRequestDto(
        String name, // 이름 or 닉네임
        String email,
        String password
) {}