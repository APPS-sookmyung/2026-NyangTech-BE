package com.nyangtech.nyangtechbackend.cat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CatInitRequest(
        @NotNull
        @Pattern(regexp = CatConstraints.CAT_NAME_REGEX, message = CatConstraints.CAT_NAME_MESSAGE)
        String catName,

        @NotNull @Positive @Max(CatConstraints.MAX_MONTHLY_BUDGET)
        Long monthlyBudget
) {
}
