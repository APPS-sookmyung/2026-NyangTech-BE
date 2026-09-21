// entity/BudgetCategory.java
package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "budget_category")
public class BudgetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long budgetId;
    private Long categoryId;
    private int amount;

    public BudgetCategory(Long budgetId, Long categoryId, int amount) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.amount = amount;
    }
    public void updateAmount(int amount) {
        this.amount = amount;
    }
}