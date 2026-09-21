package com.nyangtech.nyangtechbackend.user.repository;

import com.nyangtech.nyangtechbackend.user.domain.UserSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserId(Long userId);
}
