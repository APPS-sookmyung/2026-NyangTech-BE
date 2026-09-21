package com.nyangtech.nyangtechbackend.cat.repository;

import com.nyangtech.nyangtechbackend.cat.domain.Cat;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatRepository extends JpaRepository<Cat, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndGraduatedFalse(Long userId);

    /** 유저의 "현재 고양이" = 가장 최근에 만난 고양이 (졸업했더라도 새 고양이를 맞이하기 전까지는 그 고양이) */
    Optional<Cat> findFirstByUserIdOrderByIdDesc(Long userId);

    /** 졸업하지 않은 고양이를 조회하면서 락을 건다. (호감도 증가 등 동시 수정 보호) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cat c where c.user.id = :userId and c.graduated = false")
    Optional<Cat> findActiveByUserIdForUpdate(@Param("userId") Long userId);

    /** 내 고양이만 조회하면서 락을 건다. 남의 고양이 ID를 넣으면 빈 결과가 나온다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cat c where c.id = :catId and c.user.id = :userId")
    Optional<Cat> findByIdAndUserIdForUpdate(@Param("catId") Long catId, @Param("userId") Long userId);
}
