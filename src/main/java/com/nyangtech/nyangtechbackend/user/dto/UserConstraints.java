package com.nyangtech.nyangtechbackend.user.dto;

/**
 * 요청 검증에 쓰는 규칙 모음. 규칙을 바꿀 때 이 파일만 고치면 된다.
 */
public final class UserConstraints {

    private UserConstraints() {
    }

    /** 영문/숫자/특수문자(공백 제외) 8~64자. BCrypt는 72바이트까지만 처리하므로 ASCII로 제한해 한도를 넘지 않게 한다. */
    public static final String PASSWORD_REGEX = "^[\\x21-\\x7E]{8,64}$";
    public static final String PASSWORD_MESSAGE = "비밀번호는 공백 없이 영문/숫자/특수문자 8~64자여야 합니다.";

    /** 한글/영문/숫자/밑줄 2~10자. */
    public static final String NICKNAME_REGEX = "^[가-힣a-zA-Z0-9_]{2,10}$";
    /** 가입 요청처럼 닉네임이 선택인 곳에서 쓴다. 빈 문자열은 "없음"으로 취급한다. */
    public static final String OPTIONAL_NICKNAME_REGEX = "^([가-힣a-zA-Z0-9_]{2,10})?$";
    public static final String NICKNAME_MESSAGE = "닉네임은 공백 없이 한글/영문/숫자/밑줄 2~10자여야 합니다.";
}
