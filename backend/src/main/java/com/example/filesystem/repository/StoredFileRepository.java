package com.example.filesystem.repository;

import com.example.filesystem.entity.StoredFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findByOwnerIdAndFolderIdOrderByNameAsc(Long ownerId, Long folderId); // null folder = root
    List<StoredFile> findByFolderId(Long folderId);
}
