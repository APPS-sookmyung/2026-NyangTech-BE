package com.nyangtech.nyangtechbackend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import com.nyangtech.nyangtechbackend.global.security.JwtTokenProvider;
import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.dto.JoinRequest;
import com.nyangtech.nyangtechbackend.user.dto.JoinResponse;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "cat@example.com";
    private static final String RAW_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "{bcrypt}encoded";

    @Mock
    UserService userService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    AuthService authService;

    private User user(long id) {
        User user = User.createLocal(EMAIL, ENCODED_PASSWORD, "냥집사");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private JoinRequest request(AuthProvider provider, String email, String nickname) {
        return new JoinRequest(provider, email, RAW_PASSWORD, nickname);
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 신규_가입이면_비밀번호를_해시해서_저장하고_isNewUser_true를_준다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());
        when(userService.existsByNickname("냥집사")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userService.register(EMAIL, ENCODED_PASSWORD, "냥집사")).thenReturn(user(7L));
        when(jwtTokenProvider.issue(7L)).thenReturn("token-7");

        JoinResponse response = authService.join(request(AuthProvider.LOCAL, EMAIL, "냥집사"));

        assertThat(response.token()).isEqualTo("token-7");
        assertThat(response.isNewUser()).isTrue();
        // 원문 비밀번호가 아닌 해시값이 저장 요청으로 넘어갔는지 확인 (when 스텁이 정확히 해시값 인자에만 반응)
        verify(userService).register(EMAIL, ENCODED_PASSWORD, "냥집사");
    }

    @Test
    void 이메일은_소문자로_바꿔서_조회하고_저장한다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());
        when(userService.existsByNickname("냥집사")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userService.register(EMAIL, ENCODED_PASSWORD, "냥집사")).thenReturn(user(7L));
        when(jwtTokenProvider.issue(7L)).thenReturn("token-7");

        authService.join(request(AuthProvider.LOCAL, "Cat@Example.COM", "냥집사"));

        verify(userService).findByProviderAndEmail(AuthProvider.LOCAL, EMAIL);
        verify(userService).register(EMAIL, ENCODED_PASSWORD, "냥집사");
    }

    @Test
    void 이미_가입된_이메일이면_비밀번호가_맞을_때_로그인되고_닉네임은_무시한다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.of(user(7L)));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.issue(7L)).thenReturn("token-7");

        JoinResponse response = authService.join(request(AuthProvider.LOCAL, EMAIL, "다른닉네임"));

        assertThat(response.token()).isEqualTo("token-7");
        assertThat(response.isNewUser()).isFalse();
        verify(userService, never()).register(anyString(), anyString(), anyString());
        verify(userService, never()).existsByNickname(anyString());
    }

    @Test
    void 이미_가입된_이메일인데_비밀번호가_틀리면_INVALID_CREDENTIALS() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.of(user(7L)));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertBusinessError(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, null)),
                UserErrorCode.INVALID_CREDENTIALS);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void 새_이메일인데_닉네임이_없으면_NICKNAME_REQUIRED이고_저장하지_않는다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());

        assertBusinessError(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, null)),
                UserErrorCode.NICKNAME_REQUIRED);
        assertBusinessError(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, "")),
                UserErrorCode.NICKNAME_REQUIRED);
        verify(userService, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void 닉네임이_이미_있으면_DUPLICATE_NICKNAME이고_저장하지_않는다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());
        when(userService.existsByNickname("냥집사")).thenReturn(true);

        assertBusinessError(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, "냥집사")),
                UserErrorCode.DUPLICATE_NICKNAME);
        verify(userService, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void 소셜_로그인은_아직_지원하지_않고_아무것도_조회하지_않는다() {
        assertBusinessError(() -> authService.join(request(AuthProvider.KAKAO, EMAIL, "냥집사")),
                UserErrorCode.UNSUPPORTED_PROVIDER);
        verifyNoInteractions(userService, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void 동시_가입으로_같은_이메일이_먼저_저장됐으면_로그인으로_처리한다() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(user(7L)));
        when(userService.existsByNickname("냥집사")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userService.register(anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.issue(7L)).thenReturn("token-7");

        JoinResponse response = authService.join(request(AuthProvider.LOCAL, EMAIL, "냥집사"));

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.token()).isEqualTo("token-7");
    }

    @Test
    void 동시_가입으로_같은_닉네임이_먼저_저장됐으면_DUPLICATE_NICKNAME() {
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());
        when(userService.existsByNickname("냥집사")).thenReturn(false).thenReturn(true);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userService.register(anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertBusinessError(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, "냥집사")),
                UserErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    void 원인을_알_수_없는_제약_위반은_숨기지_않고_그대로_던진다() {
        DataIntegrityViolationException unknown = new DataIntegrityViolationException("unknown");
        when(userService.findByProviderAndEmail(AuthProvider.LOCAL, EMAIL)).thenReturn(Optional.empty());
        when(userService.existsByNickname("냥집사")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
        when(userService.register(anyString(), anyString(), anyString())).thenThrow(unknown);

        assertThatThrownBy(() -> authService.join(request(AuthProvider.LOCAL, EMAIL, "냥집사")))
                .isSameAs(unknown);
    }
}
