package com.wayble.server.wayblezone.controller;

import com.wayble.server.common.dto.FacilityDto;
import com.wayble.server.common.response.CommonResponse;
import com.wayble.server.wayblezone.dto.WaybleZoneListResponseDto;
import com.wayble.server.wayblezone.service.WaybleZoneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class WaybleZoneControllerTest {
    @Mock
    private WaybleZoneService waybleZoneService;

    @InjectMocks
    private WaybleZoneController waybleZoneController;

    @Test
    @DisplayName("웨이블존 목록 조회 - 성공")
    void t1() {
        FacilityDto facilities = FacilityDto.builder()
                .hasSlope(true)
                .hasNoDoorStep(true)
                .hasElevator(false)
                .hasTableSeat(true)
                .hasDisabledToilet(false)
                .floorInfo("1층")
                .build();

        WaybleZoneListResponseDto item1 = WaybleZoneListResponseDto.builder()
                .waybleZoneId(1L)
                .name("스타벅스 강남점")
                .category("카페")
                .address("서울 강남구 강남대로 446 (역삼동)")
                .rating(4.5)
                .reviewCount(23L)
                .imageUrl("https://image.url/wayble1.jpg")
                .contactNumber("02-558-2161")
                .facilities(facilities)
                .build();

        WaybleZoneListResponseDto item2 = WaybleZoneListResponseDto.builder()
                .waybleZoneId(2L)
                .name("메가엠지씨커피 강남중앙점")
                .category("카페")
                .address("서울특별시 서초구 서초대로77길 35, 1층 102호 (서초동)")
                .rating(4.2)
                .reviewCount(520L)
                .imageUrl("https://image.url/wayble1.jpg")
                .contactNumber("02-533-0656")
                .facilities(facilities)
                .build();

        when(waybleZoneService.getWaybleZones("서초구", "카페"))
                .thenReturn(List.of(item1, item2));

        CommonResponse<List<WaybleZoneListResponseDto>> response = waybleZoneController.getWaybleZoneList("서초구", "카페");

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        WaybleZoneListResponseDto first = response.getData().get(0);
        assertEquals(1L, first.waybleZoneId());
        assertEquals("스타벅스 강남점", first.name());
        assertEquals("카페", first.category());
        assertTrue(first.address().contains("강남대로"));
        assertEquals(4.5, first.rating(), 1e-6);
        assertEquals(23L, first.reviewCount());
        assertEquals("https://image.url/wayble1.jpg", first.imageUrl());
        assertEquals("02-558-2161", first.contactNumber());
        assertNotNull(first.facilities());
        assertTrue(first.facilities().hasSlope());
        assertTrue(first.facilities().hasNoDoorStep());
        assertFalse(first.facilities().hasElevator());
        assertTrue(first.facilities().hasTableSeat());
        assertFalse(first.facilities().hasDisabledToilet());
        assertEquals("1층", first.facilities().floorInfo());

        WaybleZoneListResponseDto second = response.getData().get(1);
        assertEquals(2L, second.waybleZoneId());
        assertEquals(520L, second.reviewCount());
    }
}
