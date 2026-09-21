package com.nyangtech.nyangtechbackend.cat.repository;

import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatTypeRepository extends JpaRepository<CatType, Long> {

    boolean existsByCode(String code);

    Optional<CatType> findByCode(String code);

    /** 처음부터 고를 수 있는 기본 종류 (등록 순서대로) */
    List<CatType> findByUnlockGraduationCountIsNullOrderByIdAsc();

    /** 조건을 채워야 해금되는 희귀 종류 (등록 순서대로) */
    List<CatType> findByUnlockGraduationCountIsNotNullOrderByIdAsc();
}
