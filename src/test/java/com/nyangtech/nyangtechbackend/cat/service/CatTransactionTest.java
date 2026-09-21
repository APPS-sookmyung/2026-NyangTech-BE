package com.nyangtech.nyangtechbackend.cat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.cat.port.MonthlyBudgetRegistrar;
import com.nyangtech.nyangtechbackend.cat.repository.CatRepository;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.support.TestDataCleaner;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import com.nyangtech.nyangtechbackend.user.service.ProfileService;
import com.nyangtech.nyangtechbackend.user.service.UserService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * "중간에 실패하면 전부 취소되는가"를 검증한다. 실제로 커밋/롤백이 일어나야 하므로 @Transactional 을 쓰지 않는다.
 * 또한 축2가 MonthlyBudgetRegistrar 를 구현해 연결했을 때의 동작(호출, 실패 시 취소)도 함께 검증한다.
 */
@SpringBootTest
class CatTransactionTest {

    @TestConfiguration
    static class RegistrarConfig {
        @Bean
        RecordingRegistrar recordingRegistrar() {
            return new RecordingRegistrar();
        }
    }

    /** 축2가 만들 예산 저장 구현체를 흉내 낸다. 호출 기록을 남기고, 원하면 실패시킨다. */
    static class RecordingRegistrar implements MonthlyBudgetRegistrar {
        final List<List<Object>> calls = new CopyOnWriteArrayList<>();
        volatile boolean fail;

        @Override
        public void register(Long userId, long monthlyBudget) {
            calls.add(List.of(userId, monthlyBudget));
            if (fail) {
                throw new IllegalStateException("예산 저장 실패(테스트용)");
            }
        }
    }

    @Autowired CatService catService;
    @Autowired UserService userService;
    @Autowired ProfileService profileService;
    @Autowired CatRepository catRepository;
    @Autowired RecordingRegistrar registrar;
    @Autowired TestDataCleaner cleaner;

    @BeforeEach
    void setUp() {
        registrar.calls.clear();
        registrar.fail = false;
    }

    @AfterEach
    void tearDown() {
        cleaner.clean();
    }

    @Test
    void 연결된_예산_기능에_유저_ID와_월_예산이_전달된다() {
        Long userId = userService.register("me@example.com", "pw", "냥집사").getId();

        catService.initCat(userId, "나비", 450000);

        assertThat(registrar.calls).containsExactly(List.of(userId, 450000L));
    }

    @Test
    void 이미_고양이가_있어_거절된_요청은_예산_기능을_호출하지_않는다() {
        Long userId = userService.register("me@example.com", "pw", "냥집사").getId();
        catService.initCat(userId, "나비", 450000);
        registrar.calls.clear();

        assertThatThrownBy(() -> catService.initCat(userId, "또나비", 999999)).isInstanceOf(BusinessException.class);

        assertThat(registrar.calls).isEmpty();
    }

    @Test
    void 예산_저장이_실패하면_고양이_생성도_함께_취소된다() {
        Long userId = userService.register("me@example.com", "pw", "냥집사").getId();
        registrar.fail = true;

        assertThatThrownBy(() -> catService.initCat(userId, "나비", 450000))
                .isInstanceOf(IllegalStateException.class);

        assertThat(catRepository.existsByUserId(userId)).isFalse();
    }

    @Test
    void 닉네임_변경이_실패하면_같이_보낸_고양이_이름_변경도_취소된다() {
        Long userId = userService.register("me@example.com", "pw", "원래닉").getId();
        userService.register("other@example.com", "pw", "남닉네임");
        catService.initCat(userId, "나비", 100000);

        assertThatThrownBy(() -> profileService.update(userId, "남닉네임", "새고양이"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.DUPLICATE_NICKNAME));

        assertThat(catService.findCurrentCatName(userId)).contains("나비");
        assertThat(userService.getUser(userId).getNickname()).isEqualTo("원래닉");
    }
}
