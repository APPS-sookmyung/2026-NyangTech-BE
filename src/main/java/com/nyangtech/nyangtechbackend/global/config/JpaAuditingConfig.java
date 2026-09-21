package com.nyangtech.nyangtechbackend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseEntity의 createdAt/updatedAt 자동 기록 기능을 켠다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
