package com.nyangtech.nyangtechbackend.cat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.cat.dto.CatStatusResponse;
import com.nyangtech.nyangtechbackend.cat.exception.CatErrorCode;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import com.nyangtech.nyangtechbackend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 다른 도메인(축2 등)이 직접 호출하게 될 CatService 창구들을 검증한다. */
@SpringBootTest
@Transactional
class CatServiceIntegrationTest {

    @Autowired CatService catService;
    @Autowired UserService userService;
    @Autowired CatTypeRepository catTypeRepository;

    private Long newUser(String name) {
        return userService.register(name + "@example.com", "pw", name).getId();
    }

    private Long newUserWithCat(String name) {
        Long userId = newUser(name);
        catService.initCat(userId, "나비", 100000);
        return userId;
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 호감도를_올리면_현재_고양이의_상태에_반영된다() {
        Long userId = newUserWithCat("me");

        catService.increaseAffection(userId, 30);
        catService.increaseAffection(userId, 80);

        CatStatusResponse status = catService.getStatus(userId);
        assertThat(status.affection()).isEqualTo(110);
        assertThat(status.level()).isEqualTo(2);
    }

    @Test
    void 고양이가_없는_유저에게는_아무_일도_일어나지_않고_예외도_없다() {
        Long userId = newUser("me");

        assertThatCode(() -> catService.increaseAffection(userId, 10)).doesNotThrowAnyException();
    }

    @Test
    void 증가량이_0_이하이면_고양이가_없어도_예외다() {
        Long userId = newUser("me");

        assertBusinessError(() -> catService.increaseAffection(userId, 0), CatErrorCode.INVALID_AFFECTION_AMOUNT);
        assertBusinessError(() -> catService.increaseAffection(userId, -5), CatErrorCode.INVALID_AFFECTION_AMOUNT);
    }

    @Test
    void 다른_유저의_고양이에는_영향을_주지_않는다() {
        Long me = newUserWithCat("me");
        Long other = newUserWithCat("other");

        catService.increaseAffection(me, 120);

        assertThat(catService.getStatus(me).affection()).isEqualTo(120);
        assertThat(catService.getStatus(other).affection()).isZero();
    }

    @Test
    void 졸업한_고양이만_있는_유저는_호감도가_오르지_않는다() {
        Long userId = newUserWithCat("me");
        catService.increaseAffection(userId, 700);
        Long catId = catService.getStatus(userId).catId();
        catService.graduate(userId, catId);

        assertThatCode(() -> catService.increaseAffection(userId, 50)).doesNotThrowAnyException();

        CatStatusResponse status = catService.getStatus(userId);
        assertThat(status.isGraduated()).isTrue();
        assertThat(status.affection()).isEqualTo(700);
    }

    @Test
    void 새_고양이를_맞이한_뒤에는_새_고양이만_호감도가_오르고_졸업한_고양이는_그대로다() {
        Long userId = newUserWithCat("me");
        catService.increaseAffection(userId, 700);
        Long firstCatId = catService.getStatus(userId).catId();
        catService.graduate(userId, firstCatId);
        Long grayId = catTypeRepository.findByCode("GRAY").orElseThrow().getId();
        catService.adopt(userId, grayId, "새냥이");

        catService.increaseAffection(userId, 40);

        CatStatusResponse current = catService.getStatus(userId);
        assertThat(current.catId()).isNotEqualTo(firstCatId);
        assertThat(current.affection()).isEqualTo(40);
        assertThat(current.isGraduated()).isFalse();
    }

    @Test
    void 현재_고양이_이름을_조회하고_바꿀_수_있다() {
        Long userId = newUserWithCat("me");
        assertThat(catService.findCurrentCatName(userId)).contains("나비");

        catService.renameCurrentCat(userId, "새이름");

        assertThat(catService.findCurrentCatName(userId)).contains("새이름");
    }

    @Test
    void 고양이가_없으면_이름_조회는_빈_값이고_이름_변경은_CAT_NOT_FOUND() {
        Long userId = newUser("me");

        assertThat(catService.findCurrentCatName(userId)).isEmpty();
        assertBusinessError(() -> catService.renameCurrentCat(userId, "새이름"), CatErrorCode.CAT_NOT_FOUND);
    }
}
