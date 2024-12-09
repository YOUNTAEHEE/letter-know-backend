package com.know.letter.user.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.know.letter.user.command.domain.aggregate.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
} 