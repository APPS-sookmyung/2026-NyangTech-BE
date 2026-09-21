package com.nyangtech.nyangtechbackend.cat.repository;

import com.nyangtech.nyangtechbackend.cat.domain.CatCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatCollectionRepository extends JpaRepository<CatCollection, Long> {

    long countByUserId(Long userId);
}
