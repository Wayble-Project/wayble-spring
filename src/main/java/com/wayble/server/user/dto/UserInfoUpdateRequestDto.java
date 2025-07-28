package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.UserType;
import lombok.Getter;

@Getter
public class UserInfoUpdateRequestDto {
    private String nickname;         // 변경할 닉네임 (nullable)
    private String birthDate;        // YYYY-MM-DD (nullable)
    private Gender gender;           // MALE, FEMALE, UNKNOWN (nullable)
    private UserType userType;       // GENERAL, DISABLED, COMPANION (nullable)
    private String disabilityType;   // userType이 DISABLED일 때만 값, 아니면 null
    private String mobilityAid;      // userType이 DISABLED일 때만 값, 아니면 null
}
