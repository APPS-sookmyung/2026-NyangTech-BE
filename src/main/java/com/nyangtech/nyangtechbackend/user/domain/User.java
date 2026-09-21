package com.nyangtech.nyangtechbackend.user.domain;

import com.nyangtech.nyangtechbackend.global.common.BaseEntity;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 테이블명 'user'는 DB 예약어라 사용할 수 없어 'users'로 한다.
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_provider_email", columnNames = {"provider", "email"}),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(nullable = false)
    private String email;

    /** BCrypt로 해시된 값만 저장한다. 소셜 로그인 유저는 null. */
    @Column
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private int coin = 0;

    private User(AuthProvider provider, String email, String password, String nickname) {
        this.provider = provider;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    /** 이메일/비밀번호 가입 유저. encodedPassword는 반드시 해시된 값이어야 한다. */
    public static User createLocal(String email, String encodedPassword, String nickname) {
        return new User(AuthProvider.LOCAL, email, encodedPassword, nickname);
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addCoin(int amount) {
        validatePositive(amount);
        this.coin += amount;
    }

    public void useCoin(int amount) {
        validatePositive(amount);
        if (this.coin < amount) {
            throw new BusinessException(UserErrorCode.NOT_ENOUGH_COIN);
        }
        this.coin -= amount;
    }

    private void validatePositive(int amount) {
        if (amount <= 0) {
            throw new BusinessException(UserErrorCode.INVALID_COIN_AMOUNT);
        }
    }
}
