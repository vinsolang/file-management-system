package com.example.filesystem.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {
    private final Path root;

    public StorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Saves under a random name; returns the relative storage path. */
    public String store(MultipartFile file, Long ownerId) throws IOException {
        Path dir = root.resolve(String.valueOf(ownerId));
        Files.createDirectories(dir);
        String name = UUID.randomUUID().toString();
        Files.copy(file.getInputStream(), dir.resolve(name));
        return ownerId + "/" + name;
    }

    public Path resolve(String relative) {
        Path p = root.resolve(relative).normalize();
        if (!p.startsWith(root)) throw new IllegalArgumentException("Invalid path");
        return p;
    }

    public void delete(String relative) {
        try { Files.deleteIfExists(resolve(relative)); } catch (IOException ignored) { }
    }
}
