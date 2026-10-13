package com.example.filesystem.repository;

import com.example.filesystem.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByStatusOrderByCreatedAtAsc(AccountStatus status);
}
