package com.wayble.server.user.dto;

import com.wayble.server.user.entity.DisabilityType;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.MobilityAid;
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
    private List<DisabilityType> disabilityType; // 복수 선택 가능
    private List<MobilityAid> mobilityAid;   // 복수 선택 가능
    // private String profileImageUrl; (추후 사용 가능)
}