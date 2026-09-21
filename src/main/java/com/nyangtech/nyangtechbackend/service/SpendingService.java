// service/SpendingService.java
package com.nyangtech.nyangtechbackend.service;

import com.nyangtech.nyangtechbackend.dto.*;
import com.nyangtech.nyangtechbackend.entity.Category;
import com.nyangtech.nyangtechbackend.entity.Spending;
import com.nyangtech.nyangtechbackend.repository.CategoryRepository;
import com.nyangtech.nyangtechbackend.repository.SpendingRepository;
import com.nyangtech.nyangtechbackend.dto.CalendarDayResponse;
import com.nyangtech.nyangtechbackend.entity.Spending;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class SpendingService {

    private final SpendingRepository spendingRepository;
    private final CategoryRepository categoryRepository;

    private static final int COIN_PER_RECORD = 1; // TODO: earnedCoin 정책 확정 필요
    private static final String UNCLASSIFIED_CATEGORY = "미분류"; // 0원 기록용 기본 카테고리

    @Transactional
    public SpendingResponse createSpending(Long userId, SpendingCreateRequest request) {
        Long categoryId = resolveCategoryId(userId, request.category());

        Spending spending = new Spending(userId, categoryId, request.amount(), request.date());
        spendingRepository.save(spending);

        // memo, receiptImg는 ERD(spending: id/user_id/category_id/amount/date)에 필드가 없어 저장하지 않음

        return new SpendingResponse(spending.getId(), resolveEarnedCoin(request.amount()));
    }

    @Transactional
    public SuccessResponse updateSpending(Long userId, Long spendingId, SpendingUpdateRequest request) {
        Spending spending = findOwnedSpending(userId, spendingId);

        Long categoryId = resolveCategoryId(userId, request.category());
        spending.update(categoryId, request.amount(), request.date());

        return new SuccessResponse(true);
    }

    @Transactional
    public SuccessResponse deleteSpending(Long userId, Long spendingId) {
        Spending spending = findOwnedSpending(userId, spendingId);
        spendingRepository.delete(spending);

        return new SuccessResponse(true);
    }

    @Transactional
    public ZeroSpendResponse recordZeroSpend(Long userId, ZeroSpendRequest request) {
        Long categoryId = resolveCategoryId(userId, UNCLASSIFIED_CATEGORY);
        Spending spending = new Spending(userId, categoryId, 0, request.date());
        spendingRepository.save(spending);

        return new ZeroSpendResponse(request.date(), true);
    }

    private Long resolveCategoryId(Long userId, String categoryName) {
        return categoryRepository.findByUserIdAndName(userId, categoryName)
                .map(Category::getId)
                .orElseGet(() -> categoryRepository.save(new Category(userId, categoryName)).getId());
    }

    private Spending findOwnedSpending(Long userId, Long spendingId) {
        Spending spending = spendingRepository.findById(spendingId)
                .orElseThrow(() -> new IllegalArgumentException("Spending not found: " + spendingId));

        if (!spending.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 소비 기록만 수정/삭제할 수 있습니다.");
        }
        return spending;
    }

    private int resolveEarnedCoin(int amount) {
        return COIN_PER_RECORD;
    }

    public List<CalendarDayResponse> getCalendar(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        var start = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();

        List<Spending> spendings = spendingRepository.findAllByUserIdAndDateBetween(userId, start, end);

        return spendings.stream()
                .collect(Collectors.groupingBy(
                        Spending::getDate,
                        Collectors.summingInt(Spending::getAmount)
                ))
                .entrySet().stream()
                .map(entry -> new CalendarDayResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CalendarDayResponse::date))
                .toList();
    }
}