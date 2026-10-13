package com.example.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FileManagementApplication {
    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("data")); // SQLite needs the folder to exist
        SpringApplication.run(FileManagementApplication.class, args);
    }
}
