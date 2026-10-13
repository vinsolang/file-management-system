package com.example.filesystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name = "files")
@Getter @Setter @NoArgsConstructor
public class StoredFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "storage_path", nullable = false)
    private String storagePath;
    @Column(name = "content_type")
    private String contentType;
    private long size;
    @Column(name = "folder_id")
    private Long folderId;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
