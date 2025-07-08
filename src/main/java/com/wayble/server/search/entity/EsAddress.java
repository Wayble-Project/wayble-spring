package com.wayble.server.search.entity;

import com.wayble.server.common.entity.Address;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@ToString
@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsAddress {
    private String state;
    private String city;
    private String district;
    private String streetAddress;
    private String detailAddress;

    @GeoPointField
    private GeoPoint location;

    public static EsAddress from(Address address) {
        return EsAddress.builder()
                .state(address.getState())
                .city(address.getCity())
                .district(address.getDistrict())
                .streetAddress(address.getStreetAddress())
                .detailAddress(address.getDetailAddress())
                .location(new GeoPoint(address.getLatitude(), address.getLongitude()))
                .build();
    }
}