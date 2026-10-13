package com.example.filesystem.controller;

import com.example.filesystem.dto.FolderRequest;
import com.example.filesystem.entity.*;
import com.example.filesystem.exception.ApiException;
import com.example.filesystem.repository.*;
import com.example.filesystem.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/folders")
public class FolderController {
    private final FolderRepository folders;
    private final StoredFileRepository files;
    private final StorageService storage;
    private final CurrentUserService current;

    public FolderController(FolderRepository folders, StoredFileRepository files,
                            StorageService storage, CurrentUserService current) {
        this.folders = folders; this.files = files; this.storage = storage; this.current = current;
    }

    /** Lists the folders and files inside parentId (root when omitted). */
    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Long parentId, Authentication auth) {
        User u = current.get(auth);
        List<Map<String, Object>> crumbs = new ArrayList<>();
        if (parentId != null) {
            Folder f = owned(parentId, u);
            while (f != null) {
                crumbs.add(0, Map.of("id", f.getId(), "name", f.getName()));
                f = f.getParentId() == null ? null : folders.findById(f.getParentId()).orElse(null);
            }
        }
        List<Map<String, Object>> fo = folders.findByOwnerIdAndParentIdOrderByNameAsc(u.getId(), parentId).stream()
            .map(f -> Map.<String, Object>of("id", f.getId(), "name", f.getName(), "createdAt", String.valueOf(f.getCreatedAt())))
            .toList();
        List<Map<String, Object>> fi = files.findByOwnerIdAndFolderIdOrderByNameAsc(u.getId(), parentId).stream()
            .map(f -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", f.getId()); m.put("name", f.getName()); m.put("size", f.getSize());
                m.put("contentType", f.getContentType()); m.put("uploadedAt", String.valueOf(f.getUploadedAt()));
                m.put("previewType", FileTypes.previewType(f.getName()));
                return m;
            }).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("currentFolderId", parentId);
        out.put("breadcrumb", crumbs);
        out.put("folders", fo);
        out.put("files", fi);
        return out;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody FolderRequest r, Authentication auth) {
        User u = current.get(auth);
        if (r.parentId() != null) owned(r.parentId(), u);
        Folder f = new Folder();
        f.setName(r.name().trim());
        f.setParentId(r.parentId());
        f.setOwnerId(u.getId());
        folders.save(f);
        return Map.of("id", f.getId(), "name", f.getName());
    }

    @DeleteMapping("/{id}") @Transactional
    public Map<String, String> delete(@PathVariable Long id, Authentication auth) {
        User u = current.get(auth);
        Folder f = folders.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Folder not found"));
        if (!f.getOwnerId().equals(u.getId()) && u.getRole() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed");
        deleteTree(f);
        return Map.of("message", "Folder deleted");
    }

    private void deleteTree(Folder f) {
        for (Folder child : folders.findByParentId(f.getId())) deleteTree(child);
        for (StoredFile sf : files.findByFolderId(f.getId())) { storage.delete(sf.getStoragePath()); files.delete(sf); }
        folders.delete(f);
    }

    private Folder owned(Long id, User u) {
        Folder f = folders.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Folder not found"));
        if (!f.getOwnerId().equals(u.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed");
        return f;
    }
}
