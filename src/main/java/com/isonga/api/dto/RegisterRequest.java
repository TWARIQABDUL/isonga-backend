package com.isonga.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Cell is required")
    private String cell;

    @Size(min = 10, max=14)
    @NotBlank(message = "Phone is required")
    private String phoneNumber;

    @Size(min = 16, max = 16)
    @NotBlank(message = "ID number is required")
    private String idNumber;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String role;
}

