package com.nyangtech.nyangtechbackend.cat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nyangtech.nyangtechbackend.cat.exception.CatErrorCode;
import com.nyangtech.nyangtechbackend.cat.repository.CatCollectionRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.support.ConcurrentRunner;
import com.nyangtech.nyangtechbackend.support.TestDataCleaner;
import com.nyangtech.nyangtechbackend.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * "버튼을 연타하거나 요청이 동시에 들어와도" 규칙이 지켜지는지 실제 스레드로 검증한다.
 * 동시 실행이 필요하므로 롤백(@Transactional)을 쓰지 않고, 테스트가 끝나면 만든 데이터를 직접 지운다.
 */
@SpringBootTest
class CatConcurrencyTest {

    private static final int THREADS = 8;

    @Autowired CatService catService;
    @Autowired UserService userService;
    @Autowired CatRepository catRepository;
    @Autowired CatCollectionRepository catCollectionRepository;
    @Autowired CatTypeRepository catTypeRepository;
    @Autowired TestDataCleaner cleaner;

    @AfterEach
    void tearDown() {
        cleaner.clean();
    }

    private Long newUser() {
        return userService.register("race@example.com", "pw", "동시성").getId();
    }

    private long countErrors(List<Object> results, CatErrorCode code) {
        return results.stream()
                .filter(BusinessException.class::isInstance)
                .map(BusinessException.class::cast)
                .filter(e -> e.getErrorCode() == code)
                .count();
    }

    @Test
    void 졸업_버튼을_동시에_여러_번_눌러도_한_번만_졸업하고_도감에도_한_번만_기록된다() throws Exception {
        Long userId = newUser();
        catService.initCat(userId, "나비", 100000);
        catService.increaseAffection(userId, 700);
        Long catId = catService.getStatus(userId).catId();

        List<Object> results = ConcurrentRunner.run(THREADS, () -> catService.graduate(userId, catId));

        assertThat(ConcurrentRunner.successes(results)).isEqualTo(1);
        assertThat(countErrors(results, CatErrorCode.CAT_ALREADY_GRADUATED)).isEqualTo(THREADS - 1);
        assertThat(catCollectionRepository.countByUserId(userId)).isEqualTo(1);
    }

    @Test
    void 새_고양이_맞이하기를_동시에_요청해도_함께하는_고양이는_하나뿐이다() throws Exception {
        Long userId = newUser();
        catService.initCat(userId, "나비", 100000);
        catService.increaseAffection(userId, 700);
        catService.graduate(userId, catService.getStatus(userId).catId());
        Long grayId = catTypeRepository.findByCode("GRAY").orElseThrow().getId();

        List<Object> results = ConcurrentRunner.run(THREADS, () -> catService.adopt(userId, grayId, "새냥이"));

        assertThat(ConcurrentRunner.successes(results)).isEqualTo(1);
        assertThat(countErrors(results, CatErrorCode.ACTIVE_CAT_EXISTS)).isEqualTo(THREADS - 1);
        assertThat(catRepository.count()).isEqualTo(2); // 졸업한 고양이 1 + 새 고양이 1
        assertThat(catRepository.existsByUserIdAndGraduatedFalse(userId)).isTrue();
    }

    @Test
    void 초기_고양이_설정을_동시에_요청해도_고양이는_하나만_만들어진다() throws Exception {
        Long userId = newUser();

        List<Object> results = ConcurrentRunner.run(THREADS, () -> catService.initCat(userId, "나비", 100000));

        assertThat(ConcurrentRunner.successes(results)).isEqualTo(1);
        assertThat(countErrors(results, CatErrorCode.CAT_ALREADY_EXISTS)).isEqualTo(THREADS - 1);
        assertThat(catRepository.count()).isEqualTo(1);
    }

    @Test
    void 호감도를_동시에_올려도_하나도_유실되지_않는다() throws Exception {
        Long userId = newUser();
        catService.initCat(userId, "나비", 100000);

        List<Object> results = ConcurrentRunner.run(20, () -> {
            catService.increaseAffection(userId, 1);
            return "ok";
        });

        assertThat(ConcurrentRunner.successes(results)).isEqualTo(20);
        assertThat(catService.getStatus(userId).affection()).isEqualTo(20);
    }
}
