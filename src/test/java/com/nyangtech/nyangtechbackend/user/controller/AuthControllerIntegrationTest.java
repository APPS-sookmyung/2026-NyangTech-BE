package com.nyangtech.nyangtechbackend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 실제 요청 → 컨트롤러 → 서비스 → DB → 응답까지 전체 흐름을 검증한다. (각 테스트 후 DB 변경은 롤백된다) */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired UserSettingsRepository userSettingsRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;

    private static String body(String provider, String email, String password, String nickname) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"provider\":\"").append(provider).append("\",");
        json.append("\"email\":\"").append(email).append("\",");
        json.append("\"password\":\"").append(password).append("\"");
        if (nickname != null) {
            json.append(",\"nickname\":\"").append(nickname).append("\"");
        }
        return json.append("}").toString();
    }

    private ResultActions join(String json) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/join").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions join(String email, String password, String nickname) throws Exception {
        return join(body("LOCAL", email, password, nickname));
    }

    @Test
    void 신규_가입하면_토큰과_isNewUser_true를_주고_DB에_저장된다() throws Exception {
        join("cat@example.com", "password123!", "냥집사")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());

        User saved = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("냥집사");
        assertThat(saved.getCoin()).isZero();
    }

    @Test
    void 비밀번호는_원문이_아니라_BCrypt_해시로_저장된다() throws Exception {
        join("cat@example.com", "password123!", "냥집사").andExpect(status().isOk());

        User saved = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("password123!");
        assertThat(saved.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("password123!", saved.getPassword())).isTrue();
    }

    @Test
    void 가입하면_기본_알림_설정이_함께_만들어진다() throws Exception {
        join("cat@example.com", "password123!", "냥집사").andExpect(status().isOk());

        User saved = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();
        UserSettings settings = userSettingsRepository.findByUserId(saved.getId()).orElseThrow();
        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.isOverBudgetOn()).isFalse();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    void 발급된_토큰의_subject는_가입한_유저의_ID이다() throws Exception {
        String response = join("cat@example.com", "password123!", "냥집사")
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(response, "$.data.token");

        User saved = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();
        assertThat(jwtDecoder.decode(token).getSubject()).isEqualTo(String.valueOf(saved.getId()));
    }

    @Test
    void 같은_이메일로_다시_요청하면_로그인되고_isNewUser_false이다() throws Exception {
        join("cat@example.com", "password123!", "냥집사").andExpect(status().isOk());

        join("cat@example.com", "password123!", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.isNewUser").value(false));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void 로그인할_때_보낸_닉네임은_무시되고_기존_닉네임이_유지된다() throws Exception {
        join("cat@example.com", "password123!", "냥집사").andExpect(status().isOk());

        join("cat@example.com", "password123!", "바뀌면안돼")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(false));

        User user = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();
        assertThat(user.getNickname()).isEqualTo("냥집사");
    }

    @Test
    void 이메일_대소문자가_달라도_같은_계정으로_본다() throws Exception {
        join("Cat@Example.COM", "password123!", "냥집사").andExpect(status().isOk());

        join("cat@example.com", "password123!", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(false));
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com")).isPresent();
    }

    @Test
    void 비밀번호가_틀리면_401_INVALID_CREDENTIALS() throws Exception {
        join("cat@example.com", "password123!", "냥집사").andExpect(status().isOk());

        join("cat@example.com", "wrong-password1", null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 새_이메일인데_닉네임이_없으면_400_NICKNAME_REQUIRED이고_계정이_생기지_않는다() throws Exception {
        join("cat@example.com", "password123!", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("NICKNAME_REQUIRED"));

        join("cat@example.com", "password123!", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("NICKNAME_REQUIRED"));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void 이미_쓰는_닉네임이면_409_DUPLICATE_NICKNAME이고_계정이_생기지_않는다() throws Exception {
        join("a@example.com", "password123!", "냥집사").andExpect(status().isOk());

        join("b@example.com", "password123!", "냥집사")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_NICKNAME"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void 소셜_로그인은_400_UNSUPPORTED_PROVIDER() throws Exception {
        join(body("KAKAO", "cat@example.com", "password123!", "냥집사"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_PROVIDER"));

        assertThat(userRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "cat@", "@example.com", "cat example@x.com", ""})
    void 이메일_형식이_잘못되면_400_INVALID_INPUT(String email) throws Exception {
        join(email, "password123!", "냥집사")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1!", "has space password1", "한글비밀번호입니다만", ""})
    void 비밀번호_규칙에_어긋나면_400_INVALID_INPUT(String password) throws Exception {
        join("cat@example.com", password, "냥집사")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 비밀번호가_64자를_넘으면_400() throws Exception {
        join("cat@example.com", "a".repeat(65), "냥집사")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "열한글자이름이넘어갑니다요요", "띄 어쓰기", "특수문자!", "<script>"})
    void 닉네임_규칙에_어긋나면_400_INVALID_INPUT(String nickname) throws Exception {
        join("cat@example.com", "password123!", nickname)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void provider가_없거나_모르는_값이면_400_INVALID_INPUT() throws Exception {
        join("{\"email\":\"cat@example.com\",\"password\":\"password123!\",\"nickname\":\"냥집사\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

        join(body("NAVER", "cat@example.com", "password123!", "냥집사"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 본문이_비어있거나_JSON이_아니면_400_INVALID_INPUT() throws Exception {
        mockMvc.perform(post("/api/v1/auth/join").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

        join("this is not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 응답_어디에도_비밀번호가_들어있지_않다() throws Exception {
        String response = join("cat@example.com", "password123!", "냥집사")
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("password123!");
    }
}
