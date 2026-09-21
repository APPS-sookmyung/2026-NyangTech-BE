package com.nyangtech.nyangtechbackend.cat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.cat.exception.CatErrorCode;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import com.nyangtech.nyangtechbackend.user.domain.User;
import org.junit.jupiter.api.Test;

class CatTest {

    private Cat newCat() {
        User user = User.createLocal("cat@example.com", "pw", "냥집사");
        return Cat.create(user, CatType.basic("CHEESE", "치즈냥", "/images/cats/cheese.png"), "나비");
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 새_고양이는_레벨_1_호감도_0_졸업_안_함으로_시작한다() {
        Cat cat = newCat();

        assertThat(cat.getName()).isEqualTo("나비");
        assertThat(cat.getLevel()).isEqualTo(1);
        assertThat(cat.getAffection()).isZero();
        assertThat(cat.isGraduated()).isFalse();
        assertThat(cat.canGraduate()).isFalse();
    }

    @Test
    void 호감도가_쌓이면_레벨이_함께_오른다() {
        Cat cat = newCat();

        cat.increaseAffection(99);
        assertThat(cat.getLevel()).isEqualTo(1);

        cat.increaseAffection(1);
        assertThat(cat.getAffection()).isEqualTo(100);
        assertThat(cat.getLevel()).isEqualTo(2);
    }

    @Test
    void 한_번에_여러_레벨을_건너뛸_수_있다() {
        Cat cat = newCat();

        cat.increaseAffection(460);

        assertThat(cat.getLevel()).isEqualTo(4);
    }

    @Test
    void 호감도는_상한을_넘지_않고_오버플로도_없다() {
        Cat cat = newCat();

        cat.increaseAffection(Integer.MAX_VALUE);
        cat.increaseAffection(Integer.MAX_VALUE);

        assertThat(cat.getAffection()).isEqualTo(CatGrowthPolicy.MAX_AFFECTION);
        assertThat(cat.getLevel()).isEqualTo(CatGrowthPolicy.MAX_LEVEL);
    }

    @Test
    void 호감도_증가량이_0_이하이면_예외다() {
        Cat cat = newCat();

        assertBusinessError(() -> cat.increaseAffection(0), CatErrorCode.INVALID_AFFECTION_AMOUNT);
        assertBusinessError(() -> cat.increaseAffection(-3), CatErrorCode.INVALID_AFFECTION_AMOUNT);
        assertThat(cat.getAffection()).isZero();
    }

    @Test
    void 최고_레벨이_아니면_졸업할_수_없다() {
        Cat cat = newCat();
        cat.increaseAffection(699);

        assertThat(cat.canGraduate()).isFalse();
        assertBusinessError(cat::graduate, CatErrorCode.CAT_NOT_GROWN_ENOUGH);
        assertThat(cat.isGraduated()).isFalse();
    }

    @Test
    void 최고_레벨이면_졸업할_수_있다() {
        Cat cat = newCat();
        cat.increaseAffection(700);

        assertThat(cat.canGraduate()).isTrue();
        cat.graduate();

        assertThat(cat.isGraduated()).isTrue();
        assertThat(cat.canGraduate()).isFalse();
    }

    @Test
    void 이미_졸업한_고양이는_다시_졸업할_수_없다() {
        Cat cat = newCat();
        cat.increaseAffection(700);
        cat.graduate();

        assertBusinessError(cat::graduate, CatErrorCode.CAT_ALREADY_GRADUATED);
    }

    @Test
    void 졸업한_고양이는_호감도가_오르지_않는다() {
        Cat cat = newCat();
        cat.increaseAffection(700);
        cat.graduate();

        assertBusinessError(() -> cat.increaseAffection(10), CatErrorCode.CAT_ALREADY_GRADUATED);
    }

    @Test
    void 이름을_바꿀_수_있다() {
        Cat cat = newCat();

        cat.rename("새이름");

        assertThat(cat.getName()).isEqualTo("새이름");
    }

    @Test
    void 종류는_조건이_있으면_희귀_없으면_기본이다() {
        assertThat(CatType.basic("A", "가", "/a.png").isRare()).isFalse();
        assertThat(CatType.rare("B", "나", "1마리 졸업", 1, "/b.png").isRare()).isTrue();
    }
}
