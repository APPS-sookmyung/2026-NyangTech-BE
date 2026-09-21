package com.nyangtech.nyangtechbackend.user.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * 알림 설정 변경 요청. 세 항목을 모두 보내는 "전체 수정" 방식이다.
 * remindTime은 "HH:mm" 형식이며, isRemindOn이 true일 때는 필수다. (끌 때 생략하면 기존 시간 유지)
 */
public record NotiSettingsRequest(
        @NotNull Boolean isRemindOn,
        LocalTime remindTime,
        @NotNull Boolean isOverBudgetOn
) {
}
