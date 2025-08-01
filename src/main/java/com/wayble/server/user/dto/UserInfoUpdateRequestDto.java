package com.wayble.server.user.dto;

import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.UserType;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.List;

@Getter
public class UserInfoUpdateRequestDto {
    private String nickname;         // 변경할 닉네임 (nullable)
    private String birthDate;        // YYYY-MM-DD (nullable)
    private Gender gender;           // MALE, FEMALE, UNKNOWN (nullable)
    private UserType userType;       // GENERAL, DISABLED, COMPANION (nullable)
    private List<String> disabilityType;  // userType이 DISABLED일 때만 값, 아니면 null
    private List<String> mobilityAid;      // userType이 DISABLED일 때만 값, 아니면 null
    /* 유저 이미지 수정 관련 (추후 사용 가능)
    @Pattern(regexp = "^(https?://).*", message = "올바른 URL 형식이어야 합니다.")
    private String profileImageUrl;
     */
}
