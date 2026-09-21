// repository/SpendingRepository.java
package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Spending;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpendingRepository extends JpaRepository<Spending, Long> {
    List<Spending> findAllByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}