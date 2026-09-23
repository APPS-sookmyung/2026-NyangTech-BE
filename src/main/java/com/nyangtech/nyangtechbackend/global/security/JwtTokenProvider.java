package com.nyangtech.nyangtechbackend.global.security;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * 로그인에 성공한 유저에게 줄 토큰(입장 팔찌)을 만든다.
 * 토큰 안에는 유저 ID(subject)와 발급/만료 시각만 담는다. 비밀번호 같은 민감 정보는 절대 넣지 않는다.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public String issue(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(properties.expiration()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
