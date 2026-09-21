package com.nyangtech.nyangtechbackend.user.service;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    /** 유저를 조회한다. 없으면 USER_NOT_FOUND. */
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
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

    /** 닉네임을 변경하고 변경된 닉네임을 돌려준다. 현재 닉네임과 같으면 아무것도 하지 않는다. */
    @Transactional
    public String changeNickname(Long userId, String nickname) {
        User user = getUser(userId);
        if (user.getNickname().equals(nickname)) {
            return nickname;
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        user.changeNickname(nickname);
        try {
            // 확인과 저장 사이에 다른 요청이 같은 닉네임을 먼저 가져간 경우를 여기서 바로 잡는다.
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }
        return user.getNickname();
    }

    /**
     * 유저에게 코인을 지급하고 지급 후 잔액을 돌려준다. (다른 도메인에서 호출: 소비 기록 보상, 간식 선물 등)
     * 같은 유저의 코인을 동시에 바꾸는 요청은 락으로 순서대로 처리되어 값이 유실되지 않는다.
     */
    @Transactional
    public int addCoin(Long userId, int amount) {
        User user = getUserForUpdate(userId);
        user.addCoin(amount);
        return user.getCoin();
    }

    /** 코인을 사용하고 남은 잔액을 돌려준다. (상점 구매 등) 잔액이 부족하면 NOT_ENOUGH_COIN. */
    @Transactional
    public int useCoin(Long userId, int amount) {
        User user = getUserForUpdate(userId);
        user.useCoin(amount);
        return user.getCoin();
    }

    private User getUserForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
