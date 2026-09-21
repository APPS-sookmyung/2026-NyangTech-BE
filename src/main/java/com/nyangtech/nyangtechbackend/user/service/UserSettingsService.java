package com.nyangtech.nyangtechbackend.user.service;

import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsRequest;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsResponse;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserService userService;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional
    public NotiSettingsResponse updateNoti(Long userId, NotiSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                // 가입 시 항상 만들어지지만, 혹시 없는 유저라도 오류 없이 기본값으로 만들어 이어간다.
                .orElseGet(() -> userSettingsRepository.save(UserSettings.createDefault(userService.getUser(userId))));

        settings.updateNoti(request.isRemindOn(), request.remindTime(), request.isOverBudgetOn());
        return new NotiSettingsResponse(settings.getId());
    }
}
