package com.nyangtech.nyangtechbackend.cat.dto;

public final class CatConstraints {

    private CatConstraints() {
    }

    /** 한글/영문/숫자/밑줄 1~10자. (임시 규칙) */
    public static final String CAT_NAME_REGEX = "^[가-힣a-zA-Z0-9_]{1,10}$";
    public static final String CAT_NAME_MESSAGE = "고양이 이름은 공백 없이 한글/영문/숫자/밑줄 1~10자여야 합니다.";

    /** 월 예산 상한(원). 터무니없는 값 방지용 임시 규칙. */
    public static final long MAX_MONTHLY_BUDGET = 10_000_000_000L;
}
