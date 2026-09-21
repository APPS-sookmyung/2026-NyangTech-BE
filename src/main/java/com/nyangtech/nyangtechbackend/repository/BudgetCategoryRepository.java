package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
}