package com.isonga.api.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String idNumber;
    private String fullName;
    private String email;
    private String cell;
    private String phoneNumber;
    private String password;
    private String role; // should be "USER" or "ADMIN"
}
