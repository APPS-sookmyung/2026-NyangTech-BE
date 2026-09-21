package com.nyangtech.nyangtechbackend.controller;

import com.nyangtech.nyangtechbackend.dto.CatInitRequest;
import com.nyangtech.nyangtechbackend.dto.CatInitResponse;
import com.nyangtech.nyangtechbackend.dto.SettingsResponse;
import com.nyangtech.nyangtechbackend.dto.UpdateSettingsRequest;
import com.nyangtech.nyangtechbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/cat-init")
    public CatInitResponse catInit(
            @RequestBody CatInitRequest request
    ) {
        return userService.catInit(request);
    }

    @PatchMapping("/settings/noti")
    public SettingsResponse updateSettings(
            @RequestBody UpdateSettingsRequest request
    ) {
        return userService.updateSettings(request);
    }
}