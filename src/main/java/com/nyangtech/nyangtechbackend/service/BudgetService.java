package com.nyangtech.nyangtechbackend.service;

import com.nyangtech.nyangtechbackend.dto.*;
import com.nyangtech.nyangtechbackend.entity.Budget;
import com.nyangtech.nyangtechbackend.entity.BudgetCategory;
import com.nyangtech.nyangtechbackend.entity.Category;
import com.nyangtech.nyangtechbackend.repository.BudgetCategoryRepository;
import com.nyangtech.nyangtechbackend.repository.BudgetRepository;
import com.nyangtech.nyangtechbackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BudgetCreateResponse createOrUpdateBudget(Long userId, BudgetCreateRequest request) {
        LocalDate now = LocalDate.now();

        Budget budget = budgetRepository.findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue())
                .orElseGet(() -> new Budget(userId, now.getYear(), now.getMonthValue(), request.totalAmount()));

        if (budget.getId() != null) {
            budget.updateTotalAmount(request.totalAmount());
        }

        budgetRepository.save(budget);

        return new BudgetCreateResponse(budget.getId(), budget.getTotalAmount());
    }

    @Transactional
    public List<BudgetCategoryUpdateResponse> updateBudgetCategories(Long userId, List<BudgetCategoryUpdateRequest> requests) {
        LocalDate now = LocalDate.now();

        Budget budget = budgetRepository.findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue())
                .orElseThrow(() -> new IllegalArgumentException("이번 달 예산이 먼저 설정되어야 합니다."));

        return requests.stream()
                .map(req -> upsertBudgetCategory(userId, budget.getId(), req))
                .toList();
    }

    private BudgetCategoryUpdateResponse upsertBudgetCategory(Long userId, Long budgetId, BudgetCategoryUpdateRequest request) {
        Long categoryId = resolveCategoryId(userId, request.category());

        BudgetCategory budgetCategory = budgetCategoryRepository.findByBudgetIdAndCategoryId(budgetId, categoryId)
                .orElseGet(() -> new BudgetCategory(budgetId, categoryId, request.amount()));

        if (budgetCategory.getId() != null) {
            budgetCategory.updateAmount(request.amount());
        }

        budgetCategoryRepository.save(budgetCategory);

        return new BudgetCategoryUpdateResponse(categoryId, request.amount());
    }

    private Long resolveCategoryId(Long userId, String categoryName) {
        return categoryRepository.findByUserIdAndName(userId, categoryName)
                .map(Category::getId)
                .orElseGet(() -> categoryRepository.save(new Category(userId, categoryName)).getId());
    }
}