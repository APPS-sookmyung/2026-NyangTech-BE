package com.nyangtech.nyangtechbackend.user.domain;

/**
 * 가입/로그인 수단. 현재는 LOCAL(이메일/비밀번호)만 지원한다.
 */
public enum AuthProvider {
    LOCAL,
    KAKAO,
    GOOGLE;

    public boolean isSupported() {
        return this == LOCAL;
    }
}
