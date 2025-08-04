package com.wayble.server.direction.entity;

import com.wayble.server.common.entity.Address;
import com.wayble.server.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address", nullable = false)
    private Address address;

    @Column(name = "latitude", nullable = false)
    private long latitude;

    @Column(name = "longitude", nullable = false)
    private long longitude;

    @Builder
    public Place(String name, Address address, long latitude, long longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Place of(String name, Address address, long latitude, long longitude) {
        return Place.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
