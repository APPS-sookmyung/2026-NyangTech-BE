package com.nyangtech.nyangtechbackend.cat.domain;

import com.nyangtech.nyangtechbackend.cat.exception.CatErrorCode;
import com.nyangtech.nyangtechbackend.global.common.BaseEntity;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저가 키우는 고양이. 졸업한 고양이도 기록으로 남는다. (유저당 "졸업하지 않은 고양이"는 최대 1마리)
 */
@Entity
@Table(name = "cats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cat_type_id", nullable = false)
    private CatType catType;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int affection = 0;

    @Column(name = "is_graduated", nullable = false)
    private boolean graduated = false;

    private Cat(User user, CatType catType, String name) {
        this.user = user;
        this.catType = catType;
        this.name = name;
    }

    /** 새 고양이는 항상 레벨 1, 호감도 0에서 시작한다. */
    public static Cat create(User user, CatType catType, String name) {
        return new Cat(user, catType, name);
    }

    public void rename(String name) {
        this.name = name;
    }

    /** 호감도를 올리고 레벨을 다시 계산한다. 상한을 넘지 않는다. 졸업한 고양이는 더 이상 자라지 않는다. */
    public void increaseAffection(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CatErrorCode.INVALID_AFFECTION_AMOUNT);
        }
        if (graduated) {
            throw new BusinessException(CatErrorCode.CAT_ALREADY_GRADUATED);
        }
        // int 오버플로를 피하려고 long으로 더한 뒤 상한을 적용한다.
        this.affection = (int) Math.min((long) this.affection + amount, CatGrowthPolicy.MAX_AFFECTION);
        this.level = CatGrowthPolicy.levelFor(this.affection);
    }

    public boolean canGraduate() {
        return !graduated && CatGrowthPolicy.canGraduate(level);
    }

    public void graduate() {
        if (graduated) {
            throw new BusinessException(CatErrorCode.CAT_ALREADY_GRADUATED);
        }
        if (!CatGrowthPolicy.canGraduate(level)) {
            throw new BusinessException(CatErrorCode.CAT_NOT_GROWN_ENOUGH);
        }
        this.graduated = true;
    }
}
