package com.example.filesystem.config;

import com.example.filesystem.entity.*;
import com.example.filesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner createAdmin(UserRepository users, PasswordEncoder encoder,
                                  @Value("${app.admin.name}") String name,
                                  @Value("${app.admin.email}") String email,
                                  @Value("${app.admin.password}") String password) {
        return args -> {
            if (users.existsByEmailIgnoreCase(email)) return;
            User admin = new User();
            admin.setFullName(name);
            admin.setEmail(email.toLowerCase());
            admin.setPasswordHash(encoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.APPROVED);
            users.save(admin);
            System.out.println("Default admin created: " + email);
        };
    }
}
