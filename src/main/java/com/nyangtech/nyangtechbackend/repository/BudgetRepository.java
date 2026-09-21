// repository/BudgetRepository.java
package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);
}