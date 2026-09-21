// controller/SpendingController.java
package com.nyangtech.nyangtechbackend.controller;

import com.nyangtech.nyangtechbackend.dto.*;
import com.nyangtech.nyangtechbackend.service.SpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spending")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingService spendingService;

    @PostMapping
    public SpendingResponse createSpending(@RequestParam Long userId,
                                           @RequestBody SpendingCreateRequest request) {
        return spendingService.createSpending(userId, request);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateSpending(@RequestParam Long userId,
                                          @PathVariable Long id,
                                          @RequestBody SpendingUpdateRequest request) {
        return spendingService.updateSpending(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public SuccessResponse deleteSpending(@RequestParam Long userId,
                                          @PathVariable Long id) {
        return spendingService.deleteSpending(userId, id);
    }

    @PostMapping("/zero")
    public ZeroSpendResponse recordZeroSpend(@RequestParam Long userId,
                                             @RequestBody ZeroSpendRequest request) {
        return spendingService.recordZeroSpend(userId, request);
    }

    @GetMapping("/calendar")
    public List<CalendarDayResponse> getCalendar(@RequestParam Long userId,
                                                 @RequestParam int year,
                                                 @RequestParam int month) {
        return spendingService.getCalendar(userId, year, month);
    }
}