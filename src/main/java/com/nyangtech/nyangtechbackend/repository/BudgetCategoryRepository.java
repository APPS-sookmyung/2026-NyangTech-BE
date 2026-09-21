// repository/BudgetCategoryRepository.java
package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    List<BudgetCategory> findAllByBudgetId(Long budgetId);
    Optional<BudgetCategory> findByBudgetIdAndCategoryId(Long budgetId, Long categoryId);
}