package com.nyangtech.nyangtechbackend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsRequest;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsResponse;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock
    UserService userService;

    @Mock
    UserSettingsRepository userSettingsRepository;

    @InjectMocks
    UserSettingsService userSettingsService;

    private User user() {
        User user = User.createLocal("cat@example.com", "pw", "냥집사");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private UserSettings settingsWithId(User user, long id) {
        UserSettings settings = UserSettings.createDefault(user);
        ReflectionTestUtils.setField(settings, "id", id);
        return settings;
    }

    @Test
    void 설정을_변경하고_settingsId를_돌려준다() {
        UserSettings settings = settingsWithId(user(), 10L);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        NotiSettingsResponse response = userSettingsService
                .updateNoti(1L, new NotiSettingsRequest(true, LocalTime.of(8, 0), true));

        assertThat(response.settingsId()).isEqualTo(10L);
        assertThat(settings.isRemindOn()).isTrue();
        assertThat(settings.getRemindTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(settings.isOverBudgetOn()).isTrue();
    }

    @Test
    void 리마인드를_켜는데_시간이_없으면_REMIND_TIME_REQUIRED() {
        UserSettings settings = settingsWithId(user(), 10L);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        assertThatThrownBy(() -> userSettingsService.updateNoti(1L, new NotiSettingsRequest(true, null, false)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.REMIND_TIME_REQUIRED));
    }

    @Test
    void 설정이_없는_유저는_기본값으로_만든_뒤_변경한다() {
        User user = user();
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userService.getUser(1L)).thenReturn(user);
        when(userSettingsRepository.save(any(UserSettings.class))).thenAnswer(invocation -> {
            UserSettings created = invocation.getArgument(0);
            ReflectionTestUtils.setField(created, "id", 99L);
            return created;
        });

        NotiSettingsResponse response = userSettingsService
                .updateNoti(1L, new NotiSettingsRequest(false, null, true));

        assertThat(response.settingsId()).isEqualTo(99L);
        verify(userSettingsRepository).save(any(UserSettings.class));
    }

    @Test
    void 존재하지_않는_유저이면_USER_NOT_FOUND이고_설정을_만들지_않는다() {
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userService.getUser(1L)).thenThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> userSettingsService.updateNoti(1L, new NotiSettingsRequest(false, null, false)))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND));
        verify(userSettingsRepository, never()).save(any());
    }
}
