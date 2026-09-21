package com.nyangtech.nyangtechbackend.service;

import com.nyangtech.nyangtechbackend.dto.CatInitRequest;
import com.nyangtech.nyangtechbackend.dto.CatInitResponse;
import com.nyangtech.nyangtechbackend.dto.SettingsResponse;
import com.nyangtech.nyangtechbackend.dto.UpdateSettingsRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public CatInitResponse catInit(CatInitRequest request) {

        return CatInitResponse.builder()
                .userId(1L)
                .catId(1L)
                .affection(0)
                .build();
    }

    public SettingsResponse updateSettings(
            UpdateSettingsRequest request
    ) {

        return SettingsResponse.builder()
                .settingsId(1L)
                .build();
    }
}