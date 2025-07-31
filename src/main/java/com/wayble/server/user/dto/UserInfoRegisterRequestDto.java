package com.wayble.server.user.dto;


import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class UserInfoRegisterRequestDto {
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 8, message = "닉네임은 8자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "생년월일은 필수입니다.")
    private String birthDate; // YYYY-MM-DD

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotNull(message = "유저 타입은 필수입니다.")
    private UserType userType;

    private List<String> disabilityType; // 장애 유형, (userType == DISABLED만 값 존재)

    private List<String> mobilityAid;  // 이동보조수단, (userType == DISABLED만 값 존재)

    // TODO: 현재 와이어프레임에 유저 이미지 등록하는 로직이 없어서 유저 정보 등록에서 이미지 등록 안하면 해당 필드 추후 삭제
    @Pattern(regexp = "^(https?://).*", message = "올바른 URL 형식이어야 합니다.")
    private String profileImageUrl;
}
