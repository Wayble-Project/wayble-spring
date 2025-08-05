package com.wayble.server.user.exception;

import com.wayble.server.common.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {

    USER_NOT_FOUND(404, 1001, "사용자를 찾을 수 없습니다."),
    WAYBLE_ZONE_NOT_FOUND(404, 1002, "해당 웨이블존을 찾을 수 없습니다."),
    PLACE_ALREADY_SAVED(400, 1003, "이미 저장한 장소입니다."),
    INVALID_USER_ID(400, 1004, "요청 경로의 유저 ID와 바디의 유저 ID가 일치하지 않습니다."),
    USER_ALREADY_EXISTS(400, 1005, "이미 존재하는 회원입니다."),
    INVALID_CREDENTIALS(400, 1006, "아이디 혹은 비밀번호가 잘못되었습니다."),
    FORBIDDEN(403, 1007, "권한이 없습니다."),
    KAKAO_AUTH_FAILED(401, 1008, "카카오 인증에 실패하였습니다."),
    USER_INFO_ALREADY_EXISTS(400,1009, "이미 등록된 정보가 있습니다."),
    INVALID_BIRTH_DATE(400, 1010, "생년월일 형식이 올바르지 않습니다."),
    USER_INFO_NOT_EXISTS(404,1011, "유저 정보가 존재하지 않습니다."),
    NICKNAME_REQUIRED(400, 1012,"nickname 파라미터는 필수입니다."),
    NICKNAME_DUPLICATED(409,1013, "이미 사용 중인 닉네임입니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;
}
