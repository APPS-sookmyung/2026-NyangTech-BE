package com.nyangtech.nyangtechbackend.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 여러 요청이 "동시에" 같은 유저의 코인을 바꿔도 값이 틀어지지 않는지 실제 스레드로 검증한다.
 * 동시 실행이 필요하므로 @Transactional(롤백)을 쓰지 않고, 테스트가 만든 데이터를 직접 지운다.
 */
@SpringBootTest
class UserCoinConcurrencyTest {

    private static final int THREADS = 20;

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired UserSettingsRepository userSettingsRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = userService.register("coin-race@example.com", "pw", "코인경쟁").getId();
    }

    @AfterEach
    void tearDown() {
        userSettingsRepository.deleteAll();
        userRepository.deleteById(userId);
    }

    /** 모든 작업을 같은 순간에 출발시키고, 각 작업의 결과(예외 포함)를 모아 돌려준다. */
    private List<Object> runConcurrently(Callable<Object> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < THREADS; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        return task.call();
                    } catch (Exception e) {
                        return e;
                    }
                }));
            }
            ready.await();
            start.countDown();

            List<Object> results = new ArrayList<>();
            for (Future<Object> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void 동시에_코인을_지급해도_하나도_유실되지_않는다() throws Exception {
        List<Object> results = runConcurrently(() -> userService.addCoin(userId, 1));

        assertThat(results).allSatisfy(result -> assertThat(result).isInstanceOf(Integer.class));
        assertThat(userRepository.findById(userId).orElseThrow().getCoin()).isEqualTo(THREADS);
    }

    @Test
    void 동시에_코인을_써도_잔액만큼만_성공하고_음수가_되지_않는다() throws Exception {
        userService.addCoin(userId, 10);

        List<Object> results = runConcurrently(() -> userService.useCoin(userId, 1));

        long successes = results.stream().filter(Integer.class::isInstance).count();
        long notEnough = results.stream()
                .filter(BusinessException.class::isInstance)
                .map(BusinessException.class::cast)
                .filter(e -> e.getErrorCode() == UserErrorCode.NOT_ENOUGH_COIN)
                .count();

        assertThat(successes).isEqualTo(10);
        assertThat(notEnough).isEqualTo(THREADS - 10);
        assertThat(userRepository.findById(userId).orElseThrow().getCoin()).isZero();
    }

    @Test
    void 지급과_사용이_섞여도_최종_잔액이_정확하다() throws Exception {
        userService.addCoin(userId, 100);
        AtomicCounter counter = new AtomicCounter();

        List<Object> results = runConcurrently(() -> counter.next() % 2 == 0
                ? userService.addCoin(userId, 3)
                : userService.useCoin(userId, 1));

        assertThat(results).allSatisfy(result -> assertThat(result).isInstanceOf(Integer.class));
        // 지급 10번(+30), 사용 10번(-10)
        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getCoin()).isEqualTo(100 + 10 * 3 - 10);
    }

    private static class AtomicCounter {
        private final java.util.concurrent.atomic.AtomicInteger value = new java.util.concurrent.atomic.AtomicInteger();

        int next() {
            return value.getAndIncrement();
        }
    }
}
