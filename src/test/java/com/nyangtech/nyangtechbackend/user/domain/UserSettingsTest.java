package com.nyangtech.nyangtechbackend.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class UserSettingsTest {

    private UserSettings newSettings() {
        return UserSettings.createDefault(User.createLocal("cat@example.com", "pw", "냥집사"));
    }

    @Test
    void 기본값은_알림_모두_꺼짐_그리고_21시() {
        UserSettings settings = newSettings();

        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.isOverBudgetOn()).isFalse();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    void 알림을_켜고_시간을_바꿀_수_있다() {
        UserSettings settings = newSettings();

        settings.updateNoti(true, LocalTime.of(8, 30), true);

        assertThat(settings.isRemindOn()).isTrue();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(settings.isOverBudgetOn()).isTrue();
    }

    @Test
    void 리마인드를_켤_때_시간이_없으면_예외이고_상태는_그대로다() {
        UserSettings settings = newSettings();

        assertThatThrownBy(() -> settings.updateNoti(true, null, true))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.REMIND_TIME_REQUIRED));
        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.isOverBudgetOn()).isFalse();
    }

    @Test
    void 리마인드를_끌_때_시간이_없으면_기존_시간을_유지한다() {
        UserSettings settings = newSettings();
        settings.updateNoti(true, LocalTime.of(7, 0), false);

        settings.updateNoti(false, null, false);

        assertThat(settings.isRemindOn()).isFalse();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(7, 0));
    }
}
