package com.nyangtech.nyangtechbackend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.nyangtech.nyangtechbackend.global.security.JwtTokenProvider;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 로그인이 필요한 유저 API(알림 설정, 프로필)를 실제 토큰으로 호출해 검증한다. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired UserSettingsRepository userSettingsRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    /** 가입 API로 유저를 만들고 발급된 토큰을 돌려준다. */
    private String signUp(String email, String nickname) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"LOCAL\",\"email\":\"" + email
                                + "\",\"password\":\"password123!\",\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.token");
    }

    private User userOf(String email) {
        return userRepository.findByProviderAndEmail(AuthProvider.LOCAL, email).orElseThrow();
    }

    private UserSettings settingsOf(String email) {
        return userSettingsRepository.findByUserId(userOf(email).getId()).orElseThrow();
    }

    private ResultActions patchNoti(String token, String json) throws Exception {
        return mockMvc.perform(patch("/api/v1/user/settings/noti")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // ---------------------------------------------------------------- 알림 설정

    @Test
    void 알림_설정을_변경하면_settingsId를_주고_DB가_바뀐다() throws Exception {
        String token = signUp("cat@example.com", "냥집사");

        patchNoti(token, "{\"isRemindOn\":true,\"remindTime\":\"08:30\",\"isOverBudgetOn\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settingsId").value(settingsOf("cat@example.com").getId()));

        UserSettings settings = settingsOf("cat@example.com");
        assertThat(settings.isRemindOn()).isTrue();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(settings.isOverBudgetOn()).isTrue();
    }

    @Test
    void 초_단위까지_포함한_시간_형식도_받는다() throws Exception {
        String token = signUp("cat@example.com", "냥집사");

        patchNoti(token, "{\"isRemindOn\":true,\"remindTime\":\"21:15:00\",\"isOverBudgetOn\":false}")
                .andExpect(status().isOk());

        assertThat(settingsOf("cat@example.com").getRemindTime()).isEqualTo(LocalTime.of(21, 15));
    }

    @Test
    void 리마인드를_끌_때_시간을_생략하면_기존_시간이_유지된다() throws Exception {
        String token = signUp("cat@example.com", "냥집사");
        patchNoti(token, "{\"isRemindOn\":true,\"remindTime\":\"07:00\",\"isOverBudgetOn\":false}")
                .andExpect(status().isOk());

        patchNoti(token, "{\"isRemindOn\":false,\"isOverBudgetOn\":true}")
                .andExpect(status().isOk());

        UserSettings settings = settingsOf("cat@example.com");
        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(settings.isOverBudgetOn()).isTrue();
    }

    @Test
    void 리마인드를_켜는데_시간이_없으면_400_REMIND_TIME_REQUIRED이고_설정은_그대로다() throws Exception {
        String token = signUp("cat@example.com", "냥집사");

        patchNoti(token, "{\"isRemindOn\":true,\"isOverBudgetOn\":true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REMIND_TIME_REQUIRED"));

        UserSettings settings = settingsOf("cat@example.com");
        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.isOverBudgetOn()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"remindTime\":\"08:00\",\"isOverBudgetOn\":true}",
            "{\"isRemindOn\":true,\"remindTime\":\"08:00\"}",
            "{}",
            "{\"isRemindOn\":\"yes\",\"remindTime\":\"08:00\",\"isOverBudgetOn\":true}",
            "{\"isRemindOn\":true,\"remindTime\":\"25:99\",\"isOverBudgetOn\":true}",
            "{\"isRemindOn\":true,\"remindTime\":\"아침\",\"isOverBudgetOn\":true}",
            "not json"
    })
    void 필수값이_빠졌거나_형식이_잘못되면_400_INVALID_INPUT(String json) throws Exception {
        String token = signUp("cat@example.com", "냥집사");

        patchNoti(token, json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 내_설정만_바뀌고_다른_유저의_설정은_그대로다() throws Exception {
        String myToken = signUp("me@example.com", "내닉네임");
        signUp("other@example.com", "남닉네임");

        patchNoti(myToken, "{\"isRemindOn\":true,\"remindTime\":\"06:00\",\"isOverBudgetOn\":true}")
                .andExpect(status().isOk());

        assertThat(settingsOf("me@example.com").isRemindOn()).isTrue();
        UserSettings others = settingsOf("other@example.com");
        assertThat(others.isRemindOn()).isFalse();
        assertThat(others.isOverBudgetOn()).isFalse();
        assertThat(others.getRemindTime()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    void 토큰이_없으면_401이다() throws Exception {
        mockMvc.perform(patch("/api/v1/user/settings/noti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isRemindOn\":false,\"isOverBudgetOn\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void 이미_없어진_유저의_토큰이면_404_USER_NOT_FOUND이다() throws Exception {
        String tokenOfGhost = jwtTokenProvider.issue(987_654L);

        patchNoti(tokenOfGhost, "{\"isRemindOn\":false,\"isOverBudgetOn\":false}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }
}
