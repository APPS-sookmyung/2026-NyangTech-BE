package com.nyangtech.nyangtechbackend.global.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 jwt.* 설정.
 *
 * @param secret     서명 비밀키. 비어 있으면 서버 시작 시 임시 키를 만든다. (32바이트 이상)
 * @param expiration 토큰 유효 기간. 미설정 시 7일.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration expiration) {

    public static final Duration DEFAULT_EXPIRATION = Duration.ofDays(7);

    public JwtProperties {
        if (expiration == null) {
            expiration = DEFAULT_EXPIRATION;
        }
        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalArgumentException("jwt.expiration은 0보다 커야 합니다.");
        }
    }
}
