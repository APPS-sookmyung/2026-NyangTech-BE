package com.nyangtech.nyangtechbackend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234";
    private static final String OTHER_SECRET = "other-secret-other-secret-other-secret-99";

    private final JwtConfig config = new JwtConfig();

    private JwtProperties properties(String secret) {
        return new JwtProperties(secret, Duration.ofDays(7));
    }

    private JwtEncoder encoder(String secret) {
        return config.jwtEncoder(config.jwtSecretKey(properties(secret)));
    }

    private JwtDecoder decoder(String secret) {
        return config.jwtDecoder(config.jwtSecretKey(properties(secret)));
    }

    private JwtTokenProvider provider(String secret) {
        return new JwtTokenProvider(encoder(secret), properties(secret));
    }

    @Test
    void 발급한_토큰을_검증하면_유저_ID와_7일_만료가_들어있다() {
        String token = provider(SECRET).issue(42L);

        Jwt jwt = decoder(SECRET).decode(token);

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void 토큰에_비밀번호_같은_다른_정보는_들어있지_않다() {
        Jwt jwt = decoder(SECRET).decode(provider(SECRET).issue(1L));

        assertThat(jwt.getClaims().keySet()).containsExactlyInAnyOrder("sub", "iat", "exp");
    }

    @Test
    void 내용을_조작한_토큰은_거절된다() {
        String token = provider(SECRET).issue(1L);
        String[] parts = token.split("\\.");
        // payload(가운데)를 다른 유저 ID(2)로 바꿔치기한다. 서명은 그대로 둔다.
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"2\",\"iat\":" + Instant.now().getEpochSecond()
                        + ",\"exp\":" + Instant.now().plusSeconds(3600).getEpochSecond() + "}").getBytes());
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThatThrownBy(() -> decoder(SECRET).decode(forged)).isInstanceOf(JwtException.class);
    }

    @Test
    void 다른_비밀키로_만든_토큰은_거절된다() {
        String token = provider(OTHER_SECRET).issue(1L);

        assertThatThrownBy(() -> decoder(SECRET).decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void 만료된_토큰은_거절된다() {
        Instant past = Instant.now().minusSeconds(10);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(past.minusSeconds(60))
                .expiresAt(past)
                .build();
        String expired = encoder(SECRET)
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThatThrownBy(() -> decoder(SECRET).decode(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    void 형식이_아닌_문자열은_거절된다() {
        assertThatThrownBy(() -> decoder(SECRET).decode("garbage")).isInstanceOf(JwtException.class);
    }

    @Test
    void 비밀키가_32바이트보다_짧으면_서버_시작을_거부한다() {
        assertThatThrownBy(() -> config.jwtSecretKey(properties("too-short")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
    }

    @Test
    void 비밀키가_비어_있으면_임시_키가_만들어지고_매번_다르다() {
        SecretKey first = config.jwtSecretKey(properties(""));
        SecretKey second = config.jwtSecretKey(properties(null));

        assertThat(first.getEncoded()).hasSize(32);
        assertThat(first.getEncoded()).isNotEqualTo(second.getEncoded());
    }

    @Test
    void 만료_기간이_0_이하이면_설정_자체가_거부된다() {
        assertThatThrownBy(() -> new JwtProperties(SECRET, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtProperties(SECRET, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 만료_기간을_생략하면_7일이다() {
        assertThat(new JwtProperties(SECRET, null).expiration()).isEqualTo(Duration.ofDays(7));
    }
}
