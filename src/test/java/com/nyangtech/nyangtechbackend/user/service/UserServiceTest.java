package com.nyangtech.nyangtechbackend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserSettingsRepository userSettingsRepository;

    @InjectMocks
    UserService userService;

    private User user() {
        return User.createLocal("cat@example.com", "pw", "냥집사");
    }

    private void assertBusinessError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    // ---- 닉네임

    @Test
    void 닉네임을_변경한다() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("새닉네임")).thenReturn(false);

        String result = userService.changeNickname(1L, "새닉네임");

        assertThat(result).isEqualTo("새닉네임");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        verify(userRepository).flush();
    }

    @Test
    void 현재와_같은_닉네임이면_중복_검사_없이_그대로_돌려준다() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));

        assertThat(userService.changeNickname(1L, "냥집사")).isEqualTo("냥집사");
        verify(userRepository, never()).existsByNickname(any());
        verify(userRepository, never()).flush();
    }

    @Test
    void 이미_있는_닉네임이면_DUPLICATE_NICKNAME이고_바뀌지_않는다() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("남닉네임")).thenReturn(true);

        assertBusinessError(() -> userService.changeNickname(1L, "남닉네임"), UserErrorCode.DUPLICATE_NICKNAME);
        assertThat(user.getNickname()).isEqualTo("냥집사");
    }

    @Test
    void 확인_직후_다른_요청이_먼저_가져가_DB_제약에_걸려도_DUPLICATE_NICKNAME으로_바꿔준다() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(userRepository.existsByNickname("새닉네임")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate")).when(userRepository).flush();

        assertBusinessError(() -> userService.changeNickname(1L, "새닉네임"), UserErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    void 닉네임_변경_대상_유저가_없으면_USER_NOT_FOUND() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertBusinessError(() -> userService.changeNickname(1L, "새닉네임"), UserErrorCode.USER_NOT_FOUND);
    }

    // ---- 코인

    @Test
    void 코인을_지급하면_지급_후_잔액을_돌려준다() {
        User user = user();
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        assertThat(userService.addCoin(1L, 30)).isEqualTo(30);
        assertThat(userService.addCoin(1L, 5)).isEqualTo(35);
    }

    @Test
    void 코인을_사용하면_남은_잔액을_돌려준다() {
        User user = user();
        user.addCoin(100);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        assertThat(userService.useCoin(1L, 40)).isEqualTo(60);
    }

    @Test
    void 잔액이_부족하면_NOT_ENOUGH_COIN이고_코인은_그대로다() {
        User user = user();
        user.addCoin(10);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        assertBusinessError(() -> userService.useCoin(1L, 11), UserErrorCode.NOT_ENOUGH_COIN);
        assertThat(user.getCoin()).isEqualTo(10);
    }

    @Test
    void 코인_대상_유저가_없으면_USER_NOT_FOUND() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertBusinessError(() -> userService.addCoin(1L, 5), UserErrorCode.USER_NOT_FOUND);
        assertBusinessError(() -> userService.useCoin(1L, 5), UserErrorCode.USER_NOT_FOUND);
    }
}
