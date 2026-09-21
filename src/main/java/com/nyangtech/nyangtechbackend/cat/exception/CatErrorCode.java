package com.nyangtech.nyangtechbackend.cat.exception;

import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CatErrorCode implements ErrorCode {

    CAT_NOT_FOUND(HttpStatus.NOT_FOUND, "고양이를 찾을 수 없습니다."),
    CAT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 고양이 종류입니다."),
    CAT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 고양이가 있습니다."),
    ACTIVE_CAT_EXISTS(HttpStatus.CONFLICT, "아직 함께하는 고양이가 있어 새 고양이를 맞이할 수 없습니다."),
    CAT_ALREADY_GRADUATED(HttpStatus.CONFLICT, "이미 졸업한 고양이입니다."),
    CAT_NOT_GROWN_ENOUGH(HttpStatus.BAD_REQUEST, "아직 졸업할 만큼 자라지 않았습니다."),
    CAT_TYPE_LOCKED(HttpStatus.FORBIDDEN, "아직 해금되지 않은 고양이 종류입니다."),
    INVALID_AFFECTION_AMOUNT(HttpStatus.BAD_REQUEST, "호감도 증가량은 1 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;
}
