package com.nyangtech.nyangtechbackend.user.service;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.global.security.JwtTokenProvider;
import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.dto.JoinRequest;
import com.nyangtech.nyangtechbackend.user.dto.JoinResponse;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 회원가입/로그인. 이 클래스는 일부러 @Transactional을 붙이지 않는다.
 * 동시 가입으로 DB 제약 위반이 나면 "실패한 트랜잭션" 밖에서 다시 조회해 처리해야 하기 때문이다.
 * (실제 저장은 UserService.register 가 자체 트랜잭션으로 수행)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public JoinResponse join(JoinRequest request) {
        if (!request.provider().isSupported()) {
            throw new BusinessException(UserErrorCode.UNSUPPORTED_PROVIDER);
        }

        String email = request.email().toLowerCase(Locale.ROOT);

        Optional<User> existing = userService.findByProviderAndEmail(AuthProvider.LOCAL, email);
        if (existing.isPresent()) {
            return login(existing.get(), request.password());
        }
        return register(email, request.password(), request.nickname());
    }

    private JoinResponse login(User user, String rawPassword) {
        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_CREDENTIALS);
        }
        return new JoinResponse(jwtTokenProvider.issue(user.getId()), false);
    }

    private JoinResponse register(String email, String rawPassword, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(UserErrorCode.NICKNAME_REQUIRED);
        }
        if (userService.existsByNickname(nickname)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        try {
            User user = userService.register(email, passwordEncoder.encode(rawPassword), nickname);
            return new JoinResponse(jwtTokenProvider.issue(user.getId()), true);
        } catch (DataIntegrityViolationException e) {
            // 위의 확인과 저장 사이에 같은 이메일/닉네임의 다른 요청이 먼저 저장된 경우
            Optional<User> raced = userService.findByProviderAndEmail(AuthProvider.LOCAL, email);
            if (raced.isPresent()) {
                return login(raced.get(), rawPassword);
            }
            if (userService.existsByNickname(nickname)) {
                throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
            }
            throw e;
        }
    }
}
