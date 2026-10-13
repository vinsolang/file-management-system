package com.example.filesystem.controller;

import com.example.filesystem.dto.AuthDtos.*;
import com.example.filesystem.entity.*;
import com.example.filesystem.exception.ApiException;
import com.example.filesystem.repository.UserRepository;
import com.example.filesystem.service.CurrentUserService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final CurrentUserService current;

    public AuthController(UserRepository users, PasswordEncoder encoder, CurrentUserService current) {
        this.users = users; this.encoder = encoder; this.current = current;
    }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest r) {
        if (users.existsByEmailIgnoreCase(r.email()))
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        User u = new User();
        u.setFullName(r.fullName().trim());
        u.setEmail(r.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.USER);
        u.setStatus(AccountStatus.PENDING);   // must be approved by an administrator
        users.save(u);
        return Map.of("message", "Registration submitted. Please wait for administrator approval.");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
        User u = users.findByEmailIgnoreCase(r.email().trim()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        // Enforced on the backend: only APPROVED accounts may log in
        if (u.getStatus() == AccountStatus.PENDING)
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account is pending administrator approval");
        if (u.getStatus() == AccountStatus.REJECTED)
            throw new ApiException(HttpStatus.FORBIDDEN, "Your registration was rejected");

        req.getSession(true);
        req.changeSessionId();
        var auth = new UsernamePasswordAuthenticationToken(u.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, req, res);
        return profile(u);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) { return profile(current.get(auth)); }

    private Map<String, Object> profile(User u) {
        return Map.of("id", u.getId(), "fullName", u.getFullName(), "email", u.getEmail(), "role", u.getRole().name());
    }
}
