package com.nyangtech.nyangtechbackend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.global.config.JpaAuditingConfig;
import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserSettingsRepository userSettingsRepository;

    @Test
    void 테이블이_실제로_만들어지고_저장_조회가_된다() {
        User saved = userRepository.saveAndFlush(User.createLocal("cat@example.com", "pw", "냥집사"));

        User found = userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "cat@example.com").orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getNickname()).isEqualTo("냥집사");
        assertThat(found.getCoin()).isZero();
    }

    @Test
    void 생성_시각과_수정_시각이_자동으로_기록된다() {
        User saved = userRepository.saveAndFlush(User.createLocal("cat@example.com", "pw", "냥집사"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void 없는_이메일은_빈_결과다() {
        assertThat(userRepository.findByProviderAndEmail(AuthProvider.LOCAL, "none@example.com")).isEmpty();
    }

    @Test
    void 같은_provider와_이메일은_중복_저장할_수_없다() {
        userRepository.saveAndFlush(User.createLocal("cat@example.com", "pw", "냥집사"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(User.createLocal("cat@example.com", "pw2", "다른닉")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 닉네임은_중복_저장할_수_없다() {
        userRepository.saveAndFlush(User.createLocal("a@example.com", "pw", "냥집사"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(User.createLocal("b@example.com", "pw", "냥집사")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 닉네임_존재_여부를_확인할_수_있다() {
        userRepository.saveAndFlush(User.createLocal("a@example.com", "pw", "냥집사"));

        assertThat(userRepository.existsByNickname("냥집사")).isTrue();
        assertThat(userRepository.existsByNickname("없는닉")).isFalse();
    }

    @Test
    void 유저당_설정은_하나만_저장할_수_있다() {
        User user = userRepository.saveAndFlush(User.createLocal("a@example.com", "pw", "냥집사"));
        userSettingsRepository.saveAndFlush(UserSettings.createDefault(user));

        assertThatThrownBy(() -> userSettingsRepository.saveAndFlush(UserSettings.createDefault(user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 유저_ID로_설정을_조회할_수_있다() {
        User user = userRepository.saveAndFlush(User.createLocal("a@example.com", "pw", "냥집사"));
        userSettingsRepository.saveAndFlush(UserSettings.createDefault(user));

        assertThat(userSettingsRepository.findByUserId(user.getId())).isPresent();
        assertThat(userSettingsRepository.findByUserId(user.getId() + 999)).isEmpty();
    }
}
