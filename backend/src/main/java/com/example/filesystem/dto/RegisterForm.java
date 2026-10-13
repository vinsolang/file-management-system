package com.example.filesystem.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterForm {
    @NotBlank @Size(max = 100) private String fullName;
    @NotBlank @Email @Size(max = 150) private String email;
    @NotBlank @Size(min = 8, max = 100) private String password;
}
