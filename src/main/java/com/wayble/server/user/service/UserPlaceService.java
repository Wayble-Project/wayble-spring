package com.wayble.server.user.service;


import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.user.dto.UserPlaceCreateRequestDto;
import com.wayble.server.user.dto.UserPlaceRequestDto;
import com.wayble.server.user.dto.UserPlaceSummaryDto;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserPlace;
import com.wayble.server.user.entity.UserPlaceWaybleZoneMapping;
import com.wayble.server.user.exception.UserErrorCase;
import com.wayble.server.user.repository.UserPlaceRepository;
import com.wayble.server.user.repository.UserPlaceWaybleZoneMappingRepository;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserPlaceService {

    private final UserRepository userRepository;
    private final WaybleZoneRepository waybleZoneRepository;
    private final UserPlaceRepository userPlaceRepository;
    private final UserPlaceWaybleZoneMappingRepository mappingRepository;

    @Transactional
    public Long createPlaceList(Long userId, UserPlaceCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        userPlaceRepository.findByUser_IdAndTitle(userId, request.title())
                .ifPresent(p -> { throw new ApplicationException(UserErrorCase.PLACE_TITLE_DUPLICATED); });

        String color = (request.color() == null || request.color().isBlank()) ? "GRAY" : request.color();

        UserPlace saved = userPlaceRepository.save(
                UserPlace.builder()
                        .title(request.title())
                        .color(color)
                        .user(user)
                        .savedCount(0)
                        .build()
        );
        return saved.getId();
    }

    @Transactional
    public void addZonesToPlace(Long userId, Long placeId, List<Long> waybleZoneIds) {
        UserPlace place = userPlaceRepository.findByIdAndUser_Id(placeId, userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.PLACE_NOT_FOUND));

        Set<Long> uniqueIds = new LinkedHashSet<>(waybleZoneIds);

        int added = 0;
        for (Long zoneId : uniqueIds) {
            WaybleZone zone = waybleZoneRepository.findById(zoneId)
                    .orElseThrow(() -> new ApplicationException(UserErrorCase.WAYBLE_ZONE_NOT_FOUND));

            boolean exists = mappingRepository.existsByUserPlace_IdAndWaybleZone_Id(placeId, zoneId);
            if (exists) continue;

            mappingRepository.save(UserPlaceWaybleZoneMapping.builder()
                    .userPlace(place)
                    .waybleZone(zone)
                    .build());

            place.increaseCount();
            zone.addLikes(1);
            waybleZoneRepository.save(zone);

            added++;
        }

        if (added > 0) userPlaceRepository.save(place);
    }


    @Transactional(readOnly = true)
    public List<UserPlaceSummaryDto> getMyPlaceSummaries(Long userId, String sort) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        List<UserPlace> places = "name".equalsIgnoreCase(sort) || "title".equalsIgnoreCase(sort)
                ? userPlaceRepository.findAllByUser_IdOrderByTitleAsc(userId)
                : userPlaceRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);

        return places.stream()
                .map(p -> UserPlaceSummaryDto.builder()
                        .placeId(p.getId())
                        .title(p.getTitle())
                        .color(p.getColor())
                        .savedCount(p.getSavedCount())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<WaybleZoneListResponseDto> getZonesInPlace(Long userId, Long placeId, int page, int size) {
        UserPlace place = userPlaceRepository.findByIdAndUser_Id(placeId, userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.PLACE_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WaybleZone> zones = mappingRepository.findZonesByPlaceId(place.getId(), pageable);

        return zones.map(z ->
                WaybleZoneListResponseDto.builder()
                        .waybleZoneId(z.getId())
                        .name(z.getZoneName())
                        .category(z.getZoneType().toString())
                        .address(z.getAddress().toFullAddress())
                        .rating(z.getRating())
                        .reviewCount(z.getReviewCount())
                        .imageUrl(z.getMainImageUrl())
                        .contactNumber(z.getContactNumber())
                        .facilities(null)
                        .build()
        );
    }

    @Transactional
    public void removeZoneFromPlace(Long userId, Long placeId, Long waybleZoneId) {
        UserPlace place = userPlaceRepository.findByIdAndUser_Id(placeId, userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.PLACE_NOT_FOUND));

        if (!mappingRepository.existsByUserPlace_IdAndWaybleZone_Id(placeId, waybleZoneId)) {
            throw new ApplicationException(UserErrorCase.PLACE_MAPPING_NOT_FOUND);
        }

        mappingRepository.deleteByUserPlace_IdAndWaybleZone_Id(placeId, waybleZoneId);

        place.decreaseCount();
        userPlaceRepository.save(place);

        waybleZoneRepository.findById(waybleZoneId).ifPresent(z -> {
            z.addLikes(-1);
            waybleZoneRepository.save(z);
        });
    }
}