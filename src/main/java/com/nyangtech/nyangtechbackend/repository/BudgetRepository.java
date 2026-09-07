package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}