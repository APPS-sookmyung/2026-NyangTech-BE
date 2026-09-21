package com.nyangtech.nyangtechbackend.cat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.nyangtech.nyangtechbackend.cat.domain.Cat;
import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import com.nyangtech.nyangtechbackend.cat.repository.CatCollectionRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import com.nyangtech.nyangtechbackend.cat.service.CatService;
import com.nyangtech.nyangtechbackend.global.security.JwtTokenProvider;
import com.nyangtech.nyangtechbackend.support.TestAuth;
import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
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

/** 로그인한 유저가 고양이를 만들고, 키우고, 졸업시키고, 새 고양이를 맞이하는 전체 흐름을 실제 API로 검증한다. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired CatRepository catRepository;
    @Autowired CatTypeRepository catTypeRepository;
    @Autowired CatCollectionRepository catCollectionRepository;
    @Autowired CatService catService;
    @Autowired JwtTokenProvider jwtTokenProvider;

    // ---------------------------------------------------------------- 도우미

    private Long userIdOf(String email) {
        return userRepository.findByProviderAndEmail(AuthProvider.LOCAL, email).orElseThrow().getId();
    }

    private ResultActions get(String token, String path) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private ResultActions postJson(String token, String path, String json) throws Exception {
        return mockMvc.perform(post(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions initCat(String token, String catName, long budget) throws Exception {
        return postJson(token, "/api/v1/user/cat-init",
                "{\"catName\":\"" + catName + "\",\"monthlyBudget\":" + budget + "}");
    }

    /** 가입하고 고양이까지 만든 뒤 {토큰, catId}를 돌려준다. */
    private Object[] signUpWithCat(String email, String nickname, String catName) throws Exception {
        String token = TestAuth.signUp(mockMvc, email, nickname);
        String response = initCat(token, catName, 300000).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number catId = JsonPath.read(response, "$.data.catId");
        return new Object[]{token, catId.longValue()};
    }

    private void grow(String email, int affection) {
        catService.increaseAffection(userIdOf(email), affection);
    }

    private ResultActions graduate(String token, long catId) throws Exception {
        return postJson(token, "/api/v1/cat/graduate", "{\"catId\":" + catId + "}");
    }

    private ResultActions adopt(String token, long catTypeId, String catName) throws Exception {
        return postJson(token, "/api/v1/cat/adopt",
                "{\"catTypeId\":" + catTypeId + ",\"catName\":\"" + catName + "\"}");
    }

    private long typeId(String code) {
        return catTypeRepository.findByCode(code).orElseThrow().getId();
    }

    private long currentCatId(String token) throws Exception {
        String response = get(token, "/api/v1/cat/status").andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.data.catId")).longValue();
    }

    // ---------------------------------------------------------------- cat-init

    @Test
    void 초기_고양이를_만들면_레벨_1_호감도_0으로_시작하고_기본_종류이다() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        initCat(token, "나비", 300000)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userIdOf("me@example.com")))
                .andExpect(jsonPath("$.data.catId").isNumber())
                .andExpect(jsonPath("$.data.affection").value(0));

        Cat cat = catRepository.findFirstByUserIdOrderByIdDesc(userIdOf("me@example.com")).orElseThrow();
        assertThat(cat.getName()).isEqualTo("나비");
        assertThat(cat.getLevel()).isEqualTo(1);
        assertThat(cat.getCatType().getCode()).isEqualTo("CHEESE");
        assertThat(cat.isGraduated()).isFalse();
    }

    @Test
    void 이미_고양이가_있으면_초기_설정은_409이고_고양이가_늘지_않는다() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");
        initCat(token, "나비", 300000).andExpect(status().isOk());

        initCat(token, "두번째", 300000)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CAT_ALREADY_EXISTS"));

        assertThat(catRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "열한글자이름이넘어갑니다요요", "띄 어쓰기", "특수문자!", "<b>"})
    void 고양이_이름_규칙에_어긋나면_400_INVALID_INPUT이고_만들어지지_않는다(String catName) throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        initCat(token, catName, 300000)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

        assertThat(catRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"catName\":\"나비\",\"monthlyBudget\":0}",
            "{\"catName\":\"나비\",\"monthlyBudget\":-1000}",
            "{\"catName\":\"나비\",\"monthlyBudget\":10000000001}",
            "{\"catName\":\"나비\",\"monthlyBudget\":\"많이\"}",
            "{\"catName\":\"나비\",\"monthlyBudget\":null}",
            "{\"catName\":\"나비\"}",
            "{\"monthlyBudget\":300000}",
            "{}",
            "not json"
    })
    void 필수값이_없거나_예산이_잘못되면_400_INVALID_INPUT(String json) throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        postJson(token, "/api/v1/user/cat-init", json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

        assertThat(catRepository.count()).isZero();
    }

    @Test
    void 토큰이_없으면_401이다() throws Exception {
        mockMvc.perform(post("/api/v1/user/cat-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catName\":\"나비\",\"monthlyBudget\":300000}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void 이미_없어진_유저의_토큰이면_404_USER_NOT_FOUND이다() throws Exception {
        initCat(jwtTokenProvider.issue(987_654L), "나비", 300000)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }

    // ---------------------------------------------------------------- status

    @Test
    void 고양이가_없으면_상태_조회는_404_CAT_NOT_FOUND() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        get(token, "/api/v1/cat/status")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CAT_NOT_FOUND"));
    }

    @Test
    void 갓_만난_고양이의_상태는_레벨_1_다음_기준_100이다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");

        get((String) me[0], "/api/v1/cat/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catId").value(me[1]))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.affection").value(0))
                .andExpect(jsonPath("$.data.nextStepMarker").value(100))
                .andExpect(jsonPath("$.data.isGraduated").value(false))
                .andExpect(jsonPath("$.data.canGraduate").value(false));
    }

    @Test
    void 호감도가_오르면_레벨과_다음_기준이_바뀐다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String token = (String) me[0];

        grow("me@example.com", 100);
        get(token, "/api/v1/cat/status")
                .andExpect(jsonPath("$.data.level").value(2))
                .andExpect(jsonPath("$.data.affection").value(100))
                .andExpect(jsonPath("$.data.nextStepMarker").value(250))
                .andExpect(jsonPath("$.data.canGraduate").value(false));
    }

    @Test
    void 최고_레벨이면_다음_기준이_null이고_졸업할_수_있다_그리고_호감도는_상한에서_멈춘다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");

        grow("me@example.com", 5000);

        get((String) me[0], "/api/v1/cat/status")
                .andExpect(jsonPath("$.data.level").value(5))
                .andExpect(jsonPath("$.data.affection").value(700))
                .andExpect(jsonPath("$.data.nextStepMarker").doesNotExist())
                .andExpect(jsonPath("$.data.canGraduate").value(true));
    }

    @Test
    void 상태_조회도_토큰이_없으면_401이다() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/cat/status"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- rare-unlock

    @Test
    void 처음에는_희귀_종류가_모두_잠겨_있고_기본_종류는_목록에_없다() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        get(token, "/api/v1/cat/rare-unlock")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].catType", contains("턱시도냥", "샴냥", "검은냥")))
                .andExpect(jsonPath("$.data[*].isUnlocked", contains(false, false, false)))
                .andExpect(jsonPath("$.data[0].conditionText").value("고양이 1마리 졸업하기"))
                .andExpect(jsonPath("$.data[0].catTypeId").value(typeId("TUXEDO")));
    }

    // ---------------------------------------------------------------- graduate

    @Test
    void 최고_레벨이_아니면_졸업할_수_없다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        grow("me@example.com", 699);

        graduate((String) me[0], (Long) me[1])
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CAT_NOT_GROWN_ENOUGH"));

        assertThat(catRepository.findById((Long) me[1]).orElseThrow().isGraduated()).isFalse();
        assertThat(catCollectionRepository.count()).isZero();
    }

    @Test
    void 졸업하면_도감에_기록되고_상태가_졸업으로_바뀐다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        grow("me@example.com", 700);

        graduate((String) me[0], (Long) me[1])
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.collectionId").isNumber())
                .andExpect(jsonPath("$.data.nextSelectionUrl").value("/cat/select"));

        assertThat(catCollectionRepository.countByUserId(userIdOf("me@example.com"))).isEqualTo(1);
        get((String) me[0], "/api/v1/cat/status")
                .andExpect(jsonPath("$.data.isGraduated").value(true))
                .andExpect(jsonPath("$.data.canGraduate").value(false));
    }

    @Test
    void 이미_졸업한_고양이를_다시_졸업시키면_409이고_도감은_늘지_않는다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        grow("me@example.com", 700);
        graduate((String) me[0], (Long) me[1]).andExpect(status().isOk());

        graduate((String) me[0], (Long) me[1])
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CAT_ALREADY_GRADUATED"));

        assertThat(catCollectionRepository.countByUserId(userIdOf("me@example.com"))).isEqualTo(1);
    }

    @Test
    void 남의_고양이나_없는_고양이는_404이고_남의_고양이는_영향이_없다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        Object[] other = signUpWithCat("other@example.com", "남집사", "루루");
        grow("other@example.com", 700);

        graduate((String) me[0], (Long) other[1])
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CAT_NOT_FOUND"));
        graduate((String) me[0], 987_654L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CAT_NOT_FOUND"));

        assertThat(catRepository.findById((Long) other[1]).orElseThrow().isGraduated()).isFalse();
        assertThat(catCollectionRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"catId\":null}", "{\"catId\":\"abc\"}", "not json"})
    void 졸업_요청의_catId가_없거나_잘못되면_400_INVALID_INPUT(String json) throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        postJson(token, "/api/v1/cat/graduate", json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    // ---------------------------------------------------------------- adopt

    @Test
    void 함께하는_고양이가_있으면_새_고양이를_맞이할_수_없다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");

        adopt((String) me[0], typeId("GRAY"), "새이름")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ACTIVE_CAT_EXISTS"));

        assertThat(catRepository.count()).isEqualTo(1);
    }

    @Test
    void 졸업하면_기본_종류의_새_고양이를_맞이할_수_있고_현재_고양이가_바뀐다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String token = (String) me[0];
        grow("me@example.com", 700);
        graduate(token, (Long) me[1]).andExpect(status().isOk());

        adopt(token, typeId("GRAY"), "새이름")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catName").value("새이름"))
                .andExpect(jsonPath("$.data.catType").value("회색냥"))
                .andExpect(jsonPath("$.data.affection").value(0));

        get(token, "/api/v1/cat/status")
                .andExpect(jsonPath("$.data.isGraduated").value(false))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.catId").value(org.hamcrest.Matchers.not((Long) me[1])));
        assertThat(catRepository.count()).isEqualTo(2);
    }

    @Test
    void 해금되지_않은_희귀_종류는_맞이할_수_없다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String token = (String) me[0];
        grow("me@example.com", 700);
        graduate(token, (Long) me[1]).andExpect(status().isOk());

        // 1마리 졸업으로는 턱시도만 열리고 샴은 아직 잠겨 있다.
        adopt(token, typeId("SIAMESE"), "샴샴")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CAT_TYPE_LOCKED"));

        assertThat(catRepository.count()).isEqualTo(1);
    }

    @Test
    void 해금된_희귀_종류는_맞이할_수_있다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String token = (String) me[0];
        grow("me@example.com", 700);
        graduate(token, (Long) me[1]).andExpect(status().isOk());

        adopt(token, typeId("TUXEDO"), "턱시")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catType").value("턱시도냥"));
    }

    @Test
    void 없는_종류는_404_CAT_TYPE_NOT_FOUND() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        adopt(token, 987_654L, "새이름")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CAT_TYPE_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"catName\":\"새이름\"}",
            "{\"catTypeId\":1}",
            "{\"catTypeId\":1,\"catName\":\"\"}",
            "{\"catTypeId\":1,\"catName\":\"띄 어쓰기\"}",
            "{\"catTypeId\":\"x\",\"catName\":\"새이름\"}",
            "{}"
    })
    void 맞이하기_요청이_잘못되면_400_INVALID_INPUT(String json) throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        postJson(token, "/api/v1/cat/adopt", json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 고양이가_전혀_없는_유저도_기본_종류는_바로_맞이할_수_있다() throws Exception {
        String token = TestAuth.signUp(mockMvc, "me@example.com", "냥집사");

        adopt(token, typeId("CHEESE"), "나비")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catType").value("치즈냥"));
    }

    // ---------------------------------------------------------------- 전체 여정

    @Test
    void 졸업을_거듭할수록_희귀_종류가_하나씩_해금된다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String token = (String) me[0];
        long catId = (Long) me[1];

        // 1마리 졸업 → 턱시도 해금
        grow("me@example.com", 700);
        graduate(token, catId).andExpect(status().isOk());
        get(token, "/api/v1/cat/rare-unlock")
                .andExpect(jsonPath("$.data[*].isUnlocked", contains(true, false, false)));

        // 2마리 졸업 → 샴 해금
        adopt(token, typeId("TUXEDO"), "턱시").andExpect(status().isOk());
        grow("me@example.com", 700);
        graduate(token, currentCatId(token)).andExpect(status().isOk());
        get(token, "/api/v1/cat/rare-unlock")
                .andExpect(jsonPath("$.data[*].isUnlocked", contains(true, true, false)));

        // 3마리 졸업 → 검은냥 해금
        adopt(token, typeId("SIAMESE"), "샴샴").andExpect(status().isOk());
        grow("me@example.com", 700);
        graduate(token, currentCatId(token)).andExpect(status().isOk());
        get(token, "/api/v1/cat/rare-unlock")
                .andExpect(jsonPath("$.data[*].isUnlocked", contains(true, true, true)));

        adopt(token, typeId("BLACK"), "까망").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.catType").value("검은냥"));
        assertThat(catCollectionRepository.countByUserId(userIdOf("me@example.com"))).isEqualTo(3);
    }

    @Test
    void 다른_유저의_해금_현황은_서로_영향을_주지_않는다() throws Exception {
        Object[] me = signUpWithCat("me@example.com", "냥집사", "나비");
        String otherToken = TestAuth.signUp(mockMvc, "other@example.com", "남집사");
        grow("me@example.com", 700);
        graduate((String) me[0], (Long) me[1]).andExpect(status().isOk());

        get(otherToken, "/api/v1/cat/rare-unlock")
                .andExpect(jsonPath("$.data[*].isUnlocked", contains(false, false, false)));
    }

    /** CatType 조회가 실제 시드 데이터를 쓰고 있는지 확인용 (테스트 자체 검증). */
    @Test
    void 테스트가_기대하는_시드_데이터가_있다() {
        assertThat(catTypeRepository.findAll()).extracting(CatType::getCode)
                .contains("CHEESE", "GRAY", "TUXEDO", "SIAMESE", "BLACK");
    }
}
