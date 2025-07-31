package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.UserType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserInfoResponseDto {
    private String nickname;
    private String birthDate;
    private Gender gender;
    private UserType userType;
    private List<String> disabilityType; // 복수 선택 가능
    private List<String> mobilityAid;    // 복수 선택 가능
    private String profileImageUrl;
}