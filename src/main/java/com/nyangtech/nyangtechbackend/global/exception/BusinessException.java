package com.nyangtech.nyangtechbackend.global.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반(코인 부족, 중복 닉네임 등)을 알릴 때 던지는 예외.
 * 예: throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
