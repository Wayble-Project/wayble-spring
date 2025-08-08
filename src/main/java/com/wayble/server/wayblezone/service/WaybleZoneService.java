package com.wayble.server.wayblezone.service;

import com.wayble.server.common.dto.FacilityDto;
import com.wayble.server.common.exception.ApplicationException;
import com.wayble.server.wayblezone.dto.WaybleZoneDetailResponseDto;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.entity.*;
import com.wayble.server.wayblezone.exception.WaybleZoneErrorCase;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WaybleZoneService {

    private final WaybleZoneRepository waybleZoneRepository;

    public List<WaybleZoneListResponseDto> getWaybleZones(String city, String category) {
        WaybleZoneType zoneType = resolveType(category);

        // fetch graph로 시설/이미지를 미리 로딩 (웨이블존 목록 조회)
        List<WaybleZone> zones = waybleZoneRepository.findSummaryByCityAndType(city, zoneType);

        return zones.stream().map(zone -> {
            WaybleZoneFacility f = zone.getFacility();
            if (f == null) {
                throw new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_FACILITY_NOT_FOUND);
            }

            WaybleZoneImage image = zone.getWaybleZoneImageList().stream().findFirst().orElse(null);

            return WaybleZoneListResponseDto.builder()
                    .waybleZoneId(zone.getId())
                    .name(zone.getZoneName())
                    .category(zone.getZoneType().toString())
                    .address(zone.getAddress().toFullAddress())
                    .rating(zone.getRating())
                    .reviewCount(zone.getReviewCount())
                    .imageUrl(image != null ? image.getImageUrl() : null)
                    .contactNumber(zone.getContactNumber())
                    .facilities(FacilityDto.builder()
                            .hasSlope(f.isHasSlope())
                            .hasNoDoorStep(f.isHasNoDoorStep())
                            .hasElevator(f.isHasElevator())
                            .hasTableSeat(f.isHasTableSeat())
                            .hasDisabledToilet(f.isHasDisabledToilet())
                            .floorInfo(f.getFloorInfo())
                            .build()
                    ).build();
        }).toList();
    }

    public WaybleZoneDetailResponseDto getWaybleZoneDetail(Long waybleZoneId) {
        // fetch graph로 시설/이미지/운영시간을 미리 로딩 (웨이블존 상세 조회)
        WaybleZone zone = waybleZoneRepository.findDetailById(waybleZoneId)
                .orElseThrow(() -> new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_NOT_FOUND));

        WaybleZoneFacility f = zone.getFacility();
        if (f == null) throw new ApplicationException(WaybleZoneErrorCase.WAYBLE_ZONE_FACILITY_NOT_FOUND);

        List<WaybleZoneImage> images = zone.getWaybleZoneImageList();
        String imageUrl = images.stream().findFirst().map(WaybleZoneImage::getImageUrl).orElse(null);
        List<String> photoUrls = images.stream().map(WaybleZoneImage::getImageUrl).toList();

        Map<String, WaybleZoneDetailResponseDto.BusinessHourDto> businessHours = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (WaybleZoneOperatingHour hour : zone.getOperatingHours()) {
            String dayKey = hour.getDayOfWeek().toString().toLowerCase();
            businessHours.put(dayKey, WaybleZoneDetailResponseDto.BusinessHourDto.builder()
                    .open(hour.getStartTime().format(formatter))
                    .close(hour.getCloseTime().format(formatter))
                    .build());
        }

        return WaybleZoneDetailResponseDto.builder()
                .waybleZoneId(zone.getId())
                .name(zone.getZoneName())
                .category(zone.getZoneType().toString())
                .address(zone.getAddress().toFullAddress())
                .rating(zone.getRating())
                .reviewCount(zone.getReviewCount())
                .contactNumber(zone.getContactNumber())
                .imageUrl(imageUrl)
                .photos(photoUrls)
                .facilities(FacilityDto.builder()
                        .hasSlope(f.isHasSlope())
                        .hasNoDoorStep(f.isHasNoDoorStep())
                        .hasElevator(f.isHasElevator())
                        .hasTableSeat(f.isHasTableSeat())
                        .hasDisabledToilet(f.isHasDisabledToilet())
                        .floorInfo(f.getFloorInfo())
                        .build())
                .businessHours(businessHours)
                .build();
    }

    private WaybleZoneType resolveType(String category) {
        if (category == null) {
            throw new ApplicationException(WaybleZoneErrorCase.INVALID_CATEGORY);
        }
        String v = category.trim().toLowerCase();
        // 한글도 가능하도록
        switch (v) {
            case "카페": return WaybleZoneType.CAFE;
            case "음식점": return WaybleZoneType.RESTAURANT;
            case "편의점": return WaybleZoneType.CONVENIENCE;
        }
        // enum이 직접 들어오는 경우
        try {
            return WaybleZoneType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(WaybleZoneErrorCase.INVALID_CATEGORY);
        }
    }
}