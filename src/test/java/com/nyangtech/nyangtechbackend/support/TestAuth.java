package com.nyangtech.nyangtechbackend.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API 통합 테스트에서 "로그인한 유저"가 필요할 때 쓰는 도우미. */
public final class TestAuth {

    private TestAuth() {
    }

    /** 회원가입 API로 유저를 만들고 발급된 토큰을 돌려준다. (비밀번호는 모두 password123!) */
    public static String signUp(MockMvc mockMvc, String email, String nickname) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"LOCAL\",\"email\":\"" + email
                                + "\",\"password\":\"password123!\",\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.token");
    }
}
