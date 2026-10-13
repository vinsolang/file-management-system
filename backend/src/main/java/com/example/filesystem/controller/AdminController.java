package com.example.filesystem.controller;

import com.example.filesystem.entity.*;
import com.example.filesystem.exception.ApiException;
import com.example.filesystem.repository.UserRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin/users")
public class AdminController {
    private final UserRepository users;
    public AdminController(UserRepository users) { this.users = users; }

    @GetMapping("/pending")
    public List<Map<String, Object>> pending() {
        return users.findByStatusOrderByCreatedAtAsc(AccountStatus.PENDING).stream()
            .map(u -> Map.<String, Object>of("id", u.getId(), "fullName", u.getFullName(),
                    "email", u.getEmail(), "createdAt", String.valueOf(u.getCreatedAt())))
            .toList();
    }

    @PatchMapping("/{id}/approve")
    public Map<String, String> approve(@PathVariable Long id) { return setStatus(id, AccountStatus.APPROVED); }

    @PatchMapping("/{id}/reject")
    public Map<String, String> reject(@PathVariable Long id) { return setStatus(id, AccountStatus.REJECTED); }

    private Map<String, String> setStatus(Long id, AccountStatus status) {
        User u = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ADMIN) throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot change an administrator");
        u.setStatus(status);
        users.save(u);
        return Map.of("message", "User " + u.getEmail() + " is now " + status);
    }
}
