package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}