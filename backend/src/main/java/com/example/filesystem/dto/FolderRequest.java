package com.example.filesystem.dto;

import jakarta.validation.constraints.*;

public record FolderRequest(
    @NotBlank @Size(max = 255) @Pattern(regexp = "[^/\\\\]+", message = "must not contain / or \\") String name,
    Long parentId) { }
