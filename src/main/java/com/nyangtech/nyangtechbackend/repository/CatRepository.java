package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatRepository extends JpaRepository<Cat, Long> {

    Optional<Cat> findByUserId(Long userId);

}