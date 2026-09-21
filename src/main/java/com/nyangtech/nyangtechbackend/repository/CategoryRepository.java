package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}