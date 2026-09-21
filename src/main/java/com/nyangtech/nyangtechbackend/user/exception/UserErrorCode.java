package com.nyangtech.nyangtechbackend.user.exception;

import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "신규 가입에는 닉네임이 필요합니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 방식입니다."),
    NOT_ENOUGH_COIN(HttpStatus.BAD_REQUEST, "코인이 부족합니다."),
    INVALID_COIN_AMOUNT(HttpStatus.BAD_REQUEST, "코인 수량은 1 이상이어야 합니다."),
    REMIND_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "리마인드 알림을 켜려면 알림 시간이 필요합니다.");

    private final HttpStatus status;
    private final String message;
}
