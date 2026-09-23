package com.nyangtech.nyangtechbackend.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.WhoAmIController.class)
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    JwtEncoder jwtEncoder;

    @Test
    void 토큰이_없으면_401이고_ApiResponse_형식이다() throws Exception {
        mockMvc.perform(get("/test/whoami"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void 올바른_토큰이면_토큰에서_유저_ID를_꺼내_쓸_수_있다() throws Exception {
        String token = jwtTokenProvider.issue(42L);

        mockMvc.perform(get("/test/whoami").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    void 가짜_토큰은_401이다() throws Exception {
        mockMvc.perform(get("/test/whoami").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void 만료된_토큰은_401이다() throws Exception {
        Instant past = Instant.now().minusSeconds(10);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("42").issuedAt(past.minusSeconds(60)).expiresAt(past).build();
        String expired = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        mockMvc.perform(get("/test/whoami").header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void Bearer_형식이_아니면_401이다() throws Exception {
        String token = jwtTokenProvider.issue(42L);

        mockMvc.perform(get("/test/whoami").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 인증_없이_열려있는_경로는_로그인_없이_접근할_수_있다() throws Exception {
        // /api/v1/auth/** 는 보안 검사를 통과하고, 실제 핸들러가 없으므로 404가 나온다. (401이 아니어야 한다)
        mockMvc.perform(get("/api/v1/auth/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @RestController
    static class WhoAmIController {

        @GetMapping("/test/whoami")
        ApiResponse<Long> whoAmI(@LoginUserId Long userId) {
            return ApiResponse.ok(userId);
        }
    }
}
