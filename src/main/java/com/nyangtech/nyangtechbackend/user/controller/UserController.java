package com.nyangtech.nyangtechbackend.user.controller;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import com.nyangtech.nyangtechbackend.global.security.LoginUserId;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsRequest;
import com.nyangtech.nyangtechbackend.user.dto.NotiSettingsResponse;
import com.nyangtech.nyangtechbackend.user.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserSettingsService userSettingsService;

    /** 알림 설정 변경 */
    @PatchMapping("/settings/noti")
    public ApiResponse<NotiSettingsResponse> updateNoti(@LoginUserId Long userId,
                                                        @Valid @RequestBody NotiSettingsRequest request) {
        return ApiResponse.ok(userSettingsService.updateNoti(userId, request));
    }
}
