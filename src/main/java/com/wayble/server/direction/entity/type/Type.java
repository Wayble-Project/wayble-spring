package com.wayble.server.direction.entity.type;

import lombok.Getter;

@Getter
public enum Type {
    WHEELCHAIR_CHARGER("휠체어 충전소"),
    NO_THRESHOLD("문턱 없음"),
    RAMP("경사로"),
    TABLE_SEAT("테이블석"),
    ELEVATOR("엘리베이터"),
    FIRST_FLOOR("1층"),
    ACCESSIBLE_TOILET("장애인 화장실"),
    WHEELCHAIR_LIFT("휠체어 리프트"),
    NONE("해당없음");

    private final String description;

    Type(String description) {
        this.description = description;
    }
}
