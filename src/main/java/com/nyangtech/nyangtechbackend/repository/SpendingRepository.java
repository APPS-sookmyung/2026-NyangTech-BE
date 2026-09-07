package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Spending;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingRepository extends JpaRepository<Spending, Long> {
}