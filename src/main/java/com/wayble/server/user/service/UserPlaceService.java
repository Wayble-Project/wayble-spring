package com.wayble.server.user.service;


import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserPlaceListResponseDto;
import com.wayble.server.user.dto.UserPlaceRequestDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserPlace;
import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserPlaceRepository;
import com.wayble.server.user.repository.UserPlaceWaybleZoneMappingRepository;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPlaceService {

    private final UserRepository userRepository;
    private final WaybleZoneRepository waybleZoneRepository;
    private final UserPlaceRepository userPlaceRepository;
    private final UserPlaceWaybleZoneMappingRepository mappingRepository;

    @Transactional
    public void saveUserPlace(Long userId, UserPlaceRequestDto request) {
        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        // 웨이블존 존재 확인
        WaybleZone waybleZone = waybleZoneRepository.findById(request.waybleZoneId())
                .orElseThrow(() -> new ApplicationException(UserErrorCase.WAYBLE_ZONE_NOT_FOUND));

        // 중복 저장 확인
        boolean alreadySaved = mappingRepository.existsByUserPlace_User_IdAndWaybleZone_Id(userId, request.waybleZoneId());
        if (alreadySaved) {
            throw new ApplicationException(UserErrorCase.PLACE_ALREADY_SAVED);
        }

        waybleZone.addLikes(1);

        // 저장
        UserPlace userPlace = userPlaceRepository.save(
                UserPlace.builder()
                        .title(request.title())
                        .user(user)
                        .build()
        );

        mappingRepository.save(
                UserPlaceWaybleZoneMapping.builder()
                        .userPlace(userPlace)
                        .waybleZone(waybleZone)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public List<UserPlaceListResponseDto> getUserPlaces(Long userId) {
        // 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        List<UserPlaceWaybleZoneMapping> mappings = mappingRepository.findAllByUserPlace_User_Id(userId);

        return mappings.stream().map(mapping -> {
            UserPlace userPlace = mapping.getUserPlace();
            WaybleZone waybleZone = mapping.getWaybleZone();

            // 웨이블존 대표 이미지 가져오기
            String imageUrl = waybleZone.getMainImageUrl();

            return UserPlaceListResponseDto.builder()
                    .placeId(userPlace.getId())
                    .title(userPlace.getTitle())
                    .waybleZone(
                            UserPlaceListResponseDto.WaybleZoneDto.builder()
                                    .waybleZoneId(waybleZone.getId())
                                    .name(waybleZone.getZoneName())
                                    .category(waybleZone.getZoneType().toString())
                                    .rating(waybleZone.getRating())
                                    .address(waybleZone.getAddress().toFullAddress())
                                    .imageUrl(imageUrl)
                                    .build()
                    )
                    .build();
        }).toList();
    }
}