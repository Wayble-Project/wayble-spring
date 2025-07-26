package com.wayble.server.user.service;


import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserInfoRegisterRequestDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserType;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserRepository userRepository;

    @Transactional
    public void registerUserInfo(Long userId, UserInfoRegisterRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        // 이미 등록된 정보가 있으면 에러 처리
        if (user.getNickname() != null) {
            throw new ApplicationException(UserErrorCase.USER_INFO_ALREADY_EXISTS);
        }

        user.setNickname(dto.getNickname());
        user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        user.setGender(dto.getGender());
        user.setUserType(dto.getUserType());

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
}
