package com.isonga.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1000")
    private BigDecimal amount;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    @Min(value = 1, message = "Minimum duration is 1 month")
    private int duration;

    @DecimalMin(value = "0.1", message = "Interest rate must be positive")
    private BigDecimal interestRate;
}
