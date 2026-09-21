package com.nyangtech.nyangtechbackend.cat.repository;

import com.nyangtech.nyangtechbackend.cat.domain.UserCatUnlock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCatUnlockRepository extends JpaRepository<UserCatUnlock, Long> {

    Optional<UserCatUnlock> findByUserIdAndCatTypeId(Long userId, Long catTypeId);

    boolean existsByUserIdAndCatTypeIdAndUnlockedTrue(Long userId, Long catTypeId);

    /** 해금된 종류의 ID 목록 */
    @Query("select u.catType.id from UserCatUnlock u where u.user.id = :userId and u.unlocked = true")
    List<Long> findUnlockedCatTypeIds(@Param("userId") Long userId);
}
