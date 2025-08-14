package com.wayble.server.wayblezone.importer;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public final class CsvSupport {
    private CsvSupport() {}

    public static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    public static boolean ynToBool(String v) {
        if (v == null) return false;
        v = v.trim();
        if (v.isEmpty()) return false;
        return "Y".equalsIgnoreCase(v);
    }

    public static Double toDouble(String s) {
        try {
            String v = nz(s).replaceAll("[^0-9.\\-]", "");
            if (v.isEmpty()) return null;
            return Double.parseDouble(v);
        } catch (Exception e) { return null; }
    }

    public static WaybleZoneType toZoneType(String smallCategory, String midCategory) {
        String c1 = nz(smallCategory);
        String c2 = nz(midCategory);
        String ref = (c1.isEmpty() ? c2 : c1).toLowerCase(Locale.KOREAN);

        if (ref.contains("카페")) return WaybleZoneType.CAFE;
        if (ref.contains("편의점")) return WaybleZoneType.CONVENIENCE;

        // 음식점 계열 키워드
        if (ref.contains("한식") || ref.contains("중식") || ref.contains("양식") ||
                ref.contains("분식") || ref.contains("일식") || ref.contains("패스트푸드") ||
                ref.contains("치킨") || ref.contains("피자") || ref.contains("요리") ||
                ref.contains("제과") || ref.contains("빵") || ref.contains("돈까스") ||
                ref.contains("고기") || ref.contains("족발") || ref.contains("국수") ||
                ref.contains("식당") ) {
            return WaybleZoneType.RESTAURANT;
        }

        // 기타는 스킵 유도: null 반환
        return null;
    }

    public static Address toAddress(String state, String city, String district, String street, String lat, String lon) {
        return Address.builder()
                .state(nz(state))
                .city(nz(city))
                .district(nz(district))
                .streetAddress(nz(street))
                .detailAddress(null)
                .latitude(toDouble(lat))
                .longitude(toDouble(lon))
                .build();
    }
}