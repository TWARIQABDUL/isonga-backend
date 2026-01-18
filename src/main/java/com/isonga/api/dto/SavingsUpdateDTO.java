package com.isonga.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SavingsUpdateDTO {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Ingoboka is required")
    @DecimalMin(value = "0.01", message = "Ingoboka must be greater than 0")
    private BigDecimal ingoboka;
}
