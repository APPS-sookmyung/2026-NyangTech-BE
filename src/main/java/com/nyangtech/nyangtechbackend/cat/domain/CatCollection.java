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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 졸업(성장 완료)한 고양이 도감 기록. */
@Entity
@Table(name = "cat_collections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatCollection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cat_type_id", nullable = false)
    private CatType catType;

    @Column(name = "graduated_at", nullable = false)
    private LocalDateTime graduatedAt;

    private CatCollection(User user, CatType catType, LocalDateTime graduatedAt) {
        this.user = user;
        this.catType = catType;
        this.graduatedAt = graduatedAt;
    }

    public static CatCollection of(User user, CatType catType, LocalDateTime graduatedAt) {
        return new CatCollection(user, catType, graduatedAt);
    }
}
