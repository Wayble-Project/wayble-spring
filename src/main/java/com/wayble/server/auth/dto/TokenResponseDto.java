package com.wayble.server.auth.dto;

public record TokenResponseDto(String accessToken, String refreshToken) {}