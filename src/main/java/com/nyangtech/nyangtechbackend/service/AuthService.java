package com.nyangtech.nyangtechbackend.service;

import com.nyangtech.nyangtechbackend.dto.JoinRequest;
import com.nyangtech.nyangtechbackend.dto.JoinResponse;
import com.nyangtech.nyangtechbackend.entity.Cat;
import com.nyangtech.nyangtechbackend.entity.User;
import com.nyangtech.nyangtechbackend.entity.UserSettings;
import com.nyangtech.nyangtechbackend.repository.CatRepository;
import com.nyangtech.nyangtechbackend.repository.UserRepository;
import com.nyangtech.nyangtechbackend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final CatRepository catRepository;

    public JoinResponse join(JoinRequest request) {

        User user = User.builder()
                .provider(request.getProvider())
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname("집사")
                .coin(0)
                .build();

        userRepository.save(user);

        UserSettings settings = UserSettings.builder()
                .userId(user.getId())
                .isRemindOn(false)
                .build();

        userSettingsRepository.save(settings);

        Cat cat = Cat.builder()
                .userId(user.getId())
                .catTypeId(1L)
                .name(request.getCatName())
                .level(1)
                .affection(0)
                .isGraduated(false)
                .build();

        catRepository.save(cat);

        return JoinResponse.builder()
                .token("test-token")
                .isNewUser(true)
                .build();
    }
}