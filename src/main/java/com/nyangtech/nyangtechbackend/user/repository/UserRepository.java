package com.nyangtech.nyangtechbackend.user.repository;

import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndEmail(AuthProvider provider, String email);

    boolean existsByNickname(String nickname);

    /**
     * 코인처럼 "읽고 → 계산하고 → 쓰는" 값을 동시에 바꿀 때 사용한다.
     * 다른 요청이 같은 유저를 수정하는 동안은 기다렸다가 순서대로 처리된다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
