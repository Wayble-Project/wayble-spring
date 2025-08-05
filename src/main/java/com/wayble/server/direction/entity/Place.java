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

    @Builder
    public Place(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public static Place of(String name, Address address) {
        return Place.builder()
                .name(name)
                .address(address)
                .build();
    }
}
