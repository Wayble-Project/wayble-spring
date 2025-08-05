package com.wayble.server.user.service;


import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserInfoRegisterRequestDto;
import com.wayble.server.user.dto.UserInfoResponseDto;
import com.wayble.server.user.dto.UserInfoUpdateRequestDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserType;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserRepository userRepository;

    @Transactional
    public void registerUserInfo(Long userId, UserInfoRegisterRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        // 이미 등록된 정보가 있으면 에러 처리
        if (user.getNickname() != null || user.getBirthDate() != null || user.getGender() != null) {
            throw new ApplicationException(UserErrorCase.USER_INFO_ALREADY_EXISTS);
        }

        user.setNickname(dto.getNickname());
        try {
            user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        } catch (DateTimeParseException e) {
            throw new ApplicationException(UserErrorCase.INVALID_BIRTH_DATE);
        }
        user.setGender(dto.getGender());
        user.setUserType(dto.getUserType());
        // (추후 사용 가능) user.updateProfileImageUrl(dto.getProfileImageUrl());

        if (dto.getUserType() == UserType.DISABLED) {
            // 장애 유형,이동보조수단 설정
            user.setDisabilityType(dto.getDisabilityType());
            user.setMobilityAid(dto.getMobilityAid());
        } else {
            user.setDisabilityType(null);
            user.setMobilityAid(null);
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateUserInfo(Long userId, UserInfoUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }

        // 생년월일 수정
        if (dto.getBirthDate() != null) {
            try {
                user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
            } catch (DateTimeParseException e) {
                throw new ApplicationException(UserErrorCase.INVALID_BIRTH_DATE);
            }
        }

        // 성별 수정
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }

        // 유저 타입 수정
        if (dto.getUserType() != null) {
            user.setUserType(dto.getUserType());
        }

        /* 유저 프로필 이미지 수정
        if (dto.getProfileImageUrl() != null) {
            user.updateProfileImageUrl(dto.getProfileImageUrl());
        }
         */

        UserType finalUserType = dto.getUserType() != null ? dto.getUserType() : user.getUserType();
        if (finalUserType == UserType.DISABLED) {
            if (dto.getDisabilityType() != null) {
                user.setDisabilityType(dto.getDisabilityType());
            }
            if (dto.getMobilityAid() != null) {
                user.setMobilityAid(dto.getMobilityAid());
            }
        } else if (dto.getUserType() != null) {
            // userType이 DISABLED가 아닌 값으로 변경된 경우에만 null로 설정
            user.setDisabilityType(null);
            user.setMobilityAid(null);
        }

        userRepository.save(user);
    }

    @Transactional
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        return UserInfoResponseDto.builder()
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .gender(user.getGender())
                .userType(user.getUserType())
                .disabilityType(user.getDisabilityType())
                .mobilityAid(user.getMobilityAid())
                // (추후 사용 가능) .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public boolean isNicknameAvailable(String nickname) {
        // DB에 동일 닉네임 존재 여부 확인
        return !userRepository.existsByNickname(nickname);
    }
}
