package com.example.filesystem.repository;

import com.example.filesystem.entity.Folder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwnerIdAndParentIdOrderByNameAsc(Long ownerId, Long parentId); // null parent = root
    List<Folder> findByParentId(Long parentId);
}
