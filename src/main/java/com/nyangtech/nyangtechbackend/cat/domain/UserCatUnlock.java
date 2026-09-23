package com.nyangtech.nyangtechbackend.cat.domain;

import com.nyangtech.nyangtechbackend.global.common.BaseEntity;
import com.nyangtech.nyangtechbackend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 유저별 희귀 고양이 종류 해금 여부. 기본 종류는 행이 없어도 항상 해금된 것으로 본다. */
@Entity
@Table(
        name = "user_cat_unlocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_cat_unlock", columnNames = {"user_id", "cat_type_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCatUnlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cat_type_id", nullable = false)
    private CatType catType;

    @Column(name = "is_unlocked", nullable = false)
    private boolean unlocked;

    private UserCatUnlock(User user, CatType catType, boolean unlocked) {
        this.user = user;
        this.catType = catType;
        this.unlocked = unlocked;
    }

    public static UserCatUnlock unlocked(User user, CatType catType) {
        return new UserCatUnlock(user, catType, true);
    }

    public void unlock() {
        this.unlocked = true;
    }
}
