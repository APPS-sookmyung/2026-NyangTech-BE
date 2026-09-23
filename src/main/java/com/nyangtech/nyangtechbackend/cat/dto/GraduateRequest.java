package com.nyangtech.nyangtechbackend.cat.dto;

import jakarta.validation.constraints.NotNull;

public record GraduateRequest(@NotNull Long catId) {
}
