package com.wayble.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Address {
    /** 시·도 */
    @Column(name = "state", length = 100, nullable = false)
    private String state;

    /** 시·군·구 */
    @Column(name = "city", length = 100, nullable = false)
    private String city;

    /** 동·읍·면 */
    @Column(name = "district", length = 100)
    private String district;

    /** 도로명 주소 */
    @Column(name = "street_address", length = 200)
    private String streetAddress;

    /** 상세 주소 */
    @Column(name = "detail_address", length = 200)
    private String detailAddress;

    /** 위도 */
    @Column(name = "latitude", columnDefinition = "DECIMAL(10,7)", nullable = false)
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude", columnDefinition = "DECIMAL(10,7)", nullable = false)
    private Double longitude;
}
