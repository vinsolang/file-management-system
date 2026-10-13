package com.example.filesystem.controller;

import com.example.filesystem.dto.AuthDtos.RegisterRequest;
import com.example.filesystem.dto.*;
import com.example.filesystem.entity.*;
import com.example.filesystem.exception.ApiException;
import com.example.filesystem.repository.StoredFileRepository;
import com.example.filesystem.service.*;
import jakarta.validation.Valid;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Thymeleaf pages. Business rules live in the REST controllers, which this class reuses. */
@Controller
public class WebController {
    private final AuthController auth;
    private final AdminController admin;
    private final FolderController folders;
    private final FileController files;
    private final StoredFileRepository fileRepo;
    private final StorageService storage;
    private final CurrentUserService current;

    public WebController(AuthController auth, AdminController admin, FolderController folders, FileController files,
                         StoredFileRepository fileRepo, StorageService storage, CurrentUserService current) {
        this.auth = auth; this.admin = admin; this.folders = folders; this.files = files;
        this.fileRepo = fileRepo; this.storage = storage; this.current = current;
    }

    /** Logged-in user, available to every template as ${me}. */
    @ModelAttribute("me")
    public User me(Authentication a) {
        if (a == null || a instanceof AnonymousAuthenticationToken || !a.isAuthenticated()) return null;
        return current.get(a);
    }

    // ---------- login / register ----------
    @GetMapping("/") public String home() { return "redirect:/files"; }
    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model m) { m.addAttribute("form", new RegisterForm()); return "register"; }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult br) {
        if (br.hasErrors()) return "register";
        try {
            auth.register(new RegisterRequest(form.getFullName(), form.getEmail(), form.getPassword()));
        } catch (ApiException e) {
            br.rejectValue("email", "duplicate", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    // ---------- files & folders ----------
    @GetMapping("/files")
    public String list(@RequestParam(required = false) Long folderId, Authentication a, Model m, RedirectAttributes ra) {
        try { m.addAttribute("data", folders.list(folderId, a)); }
        catch (ApiException e) { ra.addFlashAttribute("error", e.getMessage()); return "redirect:/files"; }
        return "files";
    }

    @PostMapping("/folders")
    public String createFolder(@RequestParam String name, @RequestParam(required = false) Long folderId,
                               Authentication a, RedirectAttributes ra) {
        String n = name.trim();
        if (n.isEmpty() || n.length() > 255 || n.contains("/") || n.contains("\\")) {
            ra.addFlashAttribute("error", "Invalid folder name (must not contain / or \\)");
        } else {
            run(ra, "Folder created", () -> folders.create(new FolderRequest(n, folderId), a));
        }
        return back(folderId);
    }

    @PostMapping("/folders/{id}/delete")
    public String deleteFolder(@PathVariable Long id, @RequestParam(required = false) Long folderId,
                               Authentication a, RedirectAttributes ra) {
        run(ra, "Folder deleted", () -> folders.delete(id, a));
        return back(folderId);
    }

    @PostMapping("/files/upload")
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Long folderId,
                         Authentication a, RedirectAttributes ra) {
        try {
            files.upload(file, folderId, a);
            ra.addFlashAttribute("success", "Uploaded " + file.getOriginalFilename());
        } catch (ApiException e) { ra.addFlashAttribute("error", e.getMessage()); }
        catch (IOException e) { ra.addFlashAttribute("error", "Upload failed"); }
        return back(folderId);
    }

    @PostMapping("/files/{id}/delete")
    public String deleteFile(@PathVariable Long id, @RequestParam(required = false) Long folderId,
                             Authentication a, RedirectAttributes ra) {
        run(ra, "File deleted", () -> files.delete(id, a));
        return back(folderId);
    }

    /** Preview page: PDF/image are embedded from the authenticated API; text is rendered here. */
    @GetMapping("/files/{id}/view")
    public String view(@PathVariable Long id, Authentication a, Model m, RedirectAttributes ra) throws IOException {
        User u = current.get(a);
        StoredFile f = fileRepo.findById(id).orElse(null);
        if (f == null || (!f.getOwnerId().equals(u.getId()) && u.getRole() != Role.ADMIN)) {
            ra.addFlashAttribute("error", "File not found");
            return "redirect:/files";
        }
        String type = FileTypes.previewType(f.getName());
        m.addAttribute("file", f);
        m.addAttribute("previewType", type);
        if ("text".equals(type)) {
            try (InputStream in = Files.newInputStream(storage.resolve(f.getStoragePath()))) {
                byte[] bytes = in.readNBytes(1_000_000);
                m.addAttribute("text", new String(bytes, StandardCharsets.UTF_8));
                m.addAttribute("truncated", f.getSize() > bytes.length);
            }
        }
        return "preview";
    }

    // ---------- administrator ----------
    @GetMapping("/admin/users")
    public String pending(Model m) { m.addAttribute("pending", admin.pending()); return "admin"; }

    @PostMapping("/admin/users/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        run(ra, "User approved", () -> admin.approve(id));
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        run(ra, "User rejected", () -> admin.reject(id));
        return "redirect:/admin/users";
    }

    // ---------- helpers ----------
    private void run(RedirectAttributes ra, String okMessage, Runnable action) {
        try { action.run(); ra.addFlashAttribute("success", okMessage); }
        catch (ApiException e) { ra.addFlashAttribute("error", e.getMessage()); }
    }
    private String back(Long folderId) { return "redirect:/files" + (folderId == null ? "" : "?folderId=" + folderId); }
}
