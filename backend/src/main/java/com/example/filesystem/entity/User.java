package com.example.filesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role = Role.USER;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AccountStatus status = AccountStatus.PENDING;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
