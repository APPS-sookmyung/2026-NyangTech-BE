package com.nyangtech.nyangtechbackend.repository;

import com.nyangtech.nyangtechbackend.  entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}