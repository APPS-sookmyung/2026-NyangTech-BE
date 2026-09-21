package com.nyangtech.nyangtechbackend.cat.domain;

import com.nyangtech.nyangtechbackend.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고양이 종류. 서버가 미리 정해두는 고정 데이터라 API로 생성하지 않고 시작 시 CatTypeSeeder가 채운다.
 * unlockGraduationCount 가 null 이면 처음부터 고를 수 있는 "기본 종류", 값이 있으면 "희귀 종류"이다.
 */
@Entity
@Table(name = "cat_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 종류를 구분하는 고정 식별자 (예: CHEESE). 시드 데이터의 중복 방지와 프론트 이미지 매핑에 쓴다. */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "type_name", nullable = false, length = 30)
    private String typeName;

    @Column(name = "unlock_condition_text")
    private String unlockConditionText;

    /** 이 수만큼 고양이를 졸업시키면 해금된다. 기본 종류는 null. */
    @Column(name = "unlock_graduation_count")
    private Integer unlockGraduationCount;

    @Column(name = "image_url")
    private String imageUrl;

    private CatType(String code, String typeName, String unlockConditionText,
                    Integer unlockGraduationCount, String imageUrl) {
        this.code = code;
        this.typeName = typeName;
        this.unlockConditionText = unlockConditionText;
        this.unlockGraduationCount = unlockGraduationCount;
        this.imageUrl = imageUrl;
    }

    public static CatType basic(String code, String typeName, String imageUrl) {
        return new CatType(code, typeName, null, null, imageUrl);
    }

    public static CatType rare(String code, String typeName, String unlockConditionText,
                               int unlockGraduationCount, String imageUrl) {
        return new CatType(code, typeName, unlockConditionText, unlockGraduationCount, imageUrl);
    }

    public boolean isRare() {
        return unlockGraduationCount != null;
    }
}
