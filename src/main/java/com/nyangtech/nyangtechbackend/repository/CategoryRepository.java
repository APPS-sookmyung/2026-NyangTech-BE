// repository/CategoryRepository.java
package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByUserId(Long userId);
    Optional<Category> findByUserIdAndName(Long userId, String name);
}