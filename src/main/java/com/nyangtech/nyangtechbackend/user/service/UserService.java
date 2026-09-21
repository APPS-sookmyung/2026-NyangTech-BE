package com.nyangtech.nyangtechbackend.user.service;

import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 데이터를 다루는 창구. 다른 도메인(cat, shop, 축2)도 유저 정보가 필요하면 Repository가 아니라 이 Service를 호출한다.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndEmail(AuthProvider provider, String email) {
        return userRepository.findByProviderAndEmail(provider, email);
    }

    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 신규 유저와 기본 알림 설정을 한 번에 저장한다. (둘 중 하나만 저장되는 일이 없도록 한 트랜잭션)
     * 이메일/닉네임 중복 시 DataIntegrityViolationException이 발생한다. → 호출한 쪽에서 처리
     */
    @Transactional
    public User register(String email, String encodedPassword, String nickname) {
        User user = userRepository.saveAndFlush(User.createLocal(email, encodedPassword, nickname));
        userSettingsRepository.save(UserSettings.createDefault(user));
        return user;
    }
}
