package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.CatType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatTypeRepository extends JpaRepository<CatType, Long> {
}