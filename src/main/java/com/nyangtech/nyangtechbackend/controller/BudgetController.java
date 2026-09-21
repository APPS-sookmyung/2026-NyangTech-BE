package com.nyangtech.nyangtechbackend.controller;

import com.nyangtech.nyangtechbackend.dto.*;
import com.nyangtech.nyangtechbackend.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public BudgetCreateResponse createBudget(@RequestParam Long userId,
                                             @RequestBody BudgetCreateRequest request) {
        return budgetService.createOrUpdateBudget(userId, request);
    }

    @PatchMapping("/categories")
    public List<BudgetCategoryUpdateResponse> updateBudgetCategories(@RequestParam Long userId,
                                                                     @RequestBody List<BudgetCategoryUpdateRequest> requests) {
        return budgetService.updateBudgetCategories(userId, requests);
    }
}