package com.isonga.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class SavingsRequest {

    // @NotBlank(message = "User ID number is required") // Use this instead of @NotNull
    @JsonIgnore
    private String userIdNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Target is required")
    @DecimalMin(value = "0.01", message = "Target must be greater than 0")
    private BigDecimal target;
}
