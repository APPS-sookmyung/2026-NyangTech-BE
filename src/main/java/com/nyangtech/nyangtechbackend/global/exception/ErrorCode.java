package com.nyangtech.nyangtechbackend.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드의 공통 규격.
 * 도메인마다 이 인터페이스를 구현한 enum을 따로 만든다. (예: UserErrorCode, CatErrorCode)
 * 이름(name())이 응답의 error.code 로 내려간다.
 */
public interface ErrorCode {

    HttpStatus getStatus();

    String name();

    String getMessage();
}
