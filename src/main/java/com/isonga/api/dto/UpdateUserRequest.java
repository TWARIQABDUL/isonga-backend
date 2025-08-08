package com.isonga.api.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phoneNumber;
    private String cell;
}
