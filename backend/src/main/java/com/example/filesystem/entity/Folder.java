package com.example.filesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name = "folders")
@Getter @Setter @NoArgsConstructor
public class Folder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
