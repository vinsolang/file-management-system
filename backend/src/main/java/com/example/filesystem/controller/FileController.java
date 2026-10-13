package com.example.filesystem.controller;

import com.example.filesystem.entity.*;
import com.example.filesystem.exception.ApiException;
import com.example.filesystem.repository.*;
import com.example.filesystem.service.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/files")
public class FileController {
    private final StoredFileRepository files;
    private final FolderRepository folders;
    private final StorageService storage;
    private final CurrentUserService current;

    public FileController(StoredFileRepository files, FolderRepository folders,
                          StorageService storage, CurrentUserService current) {
        this.files = files; this.folders = folders; this.storage = storage; this.current = current;
    }

    @PostMapping("/upload") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) Long folderId,
                                      Authentication auth) throws IOException {
        User u = current.get(auth);
        if (file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "File is empty");
        if (folderId != null) {
            Folder f = folders.findById(folderId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Folder not found"));
            if (!f.getOwnerId().equals(u.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        String name = (file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()).replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        if (name.isBlank()) name = "file";

        StoredFile sf = new StoredFile();
        sf.setName(name);
        sf.setStoragePath(storage.store(file, u.getId()));
        sf.setContentType(file.getContentType());
        sf.setSize(file.getSize());
        sf.setFolderId(folderId);
        sf.setOwnerId(u.getId());
        files.save(sf);
        return Map.of("id", sf.getId(), "name", sf.getName());
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id, Authentication auth) {
        StoredFile f = accessible(id, auth);
        MediaType type = FileTypes.previewMediaType(f.getName());
        if (type == null) throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Preview not available. Please download the file.");
        return send(f, type, ContentDisposition.inline());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication auth) {
        StoredFile f = accessible(id, auth);
        return send(f, MediaType.APPLICATION_OCTET_STREAM, ContentDisposition.attachment());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id, Authentication auth) {
        StoredFile f = accessible(id, auth);
        storage.delete(f.getStoragePath());
        files.delete(f);
        return Map.of("message", "File deleted");
    }

    private ResponseEntity<Resource> send(StoredFile f, MediaType type, ContentDisposition.Builder cd) {
        Resource res = new FileSystemResource(storage.resolve(f.getStoragePath()));
        if (!res.exists()) throw new ApiException(HttpStatus.NOT_FOUND, "File content is missing");
        return ResponseEntity.ok().contentType(type).contentLength(f.getSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, cd.filename(f.getName(), StandardCharsets.UTF_8).build().toString())
            .body(res);
    }

    /** Only the owner or an administrator may access a file. */
    private StoredFile accessible(Long id, Authentication auth) {
        User u = current.get(auth);
        StoredFile f = files.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        if (!f.getOwnerId().equals(u.getId()) && u.getRole() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed");
        return f;
    }
}
