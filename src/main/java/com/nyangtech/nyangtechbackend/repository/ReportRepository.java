// repository/ReportRepository.java
package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserIdAndMonth(Long userId, int month);
}