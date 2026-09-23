package com.nyangtech.nyangtechbackend.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import org.junit.jupiter.api.Test;

class UserTest {

    private User newUser() {
        return User.createLocal("cat@example.com", "encoded-password", "냥집사");
    }

    @Test
    void 로컬_유저는_LOCAL_provider와_코인_0으로_생성된다() {
        User user = newUser();

        assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.getEmail()).isEqualTo("cat@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getNickname()).isEqualTo("냥집사");
        assertThat(user.getCoin()).isZero();
    }

    @Test
    void 코인을_더할_수_있다() {
        User user = newUser();

        user.addCoin(30);
        user.addCoin(5);

        assertThat(user.getCoin()).isEqualTo(35);
    }

    @Test
    void 코인을_쓸_수_있고_전부_써도_된다() {
        User user = newUser();
        user.addCoin(100);

        user.useCoin(40);
        assertThat(user.getCoin()).isEqualTo(60);

        user.useCoin(60);
        assertThat(user.getCoin()).isZero();
    }

    @Test
    void 코인이_부족하면_예외이고_코인은_그대로다() {
        User user = newUser();
        user.addCoin(10);

        assertThatThrownBy(() -> user.useCoin(11))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.NOT_ENOUGH_COIN));
        assertThat(user.getCoin()).isEqualTo(10);
    }

    @Test
    void 코인_수량이_0_이하이면_예외다() {
        User user = newUser();

        assertThatThrownBy(() -> user.addCoin(0))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.INVALID_COIN_AMOUNT));
        assertThatThrownBy(() -> user.addCoin(-5))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.INVALID_COIN_AMOUNT));
        assertThatThrownBy(() -> user.useCoin(0))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.INVALID_COIN_AMOUNT));
    }

    @Test
    void 닉네임을_바꿀_수_있다() {
        User user = newUser();

        user.changeNickname("새이름");

        assertThat(user.getNickname()).isEqualTo("새이름");
    }
}
