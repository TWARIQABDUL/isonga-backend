package com.isonga.api.dto;

import com.isonga.api.models.Loan;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusUpdateRequest {

    @NotNull(message = "Loan status is required")
    private Loan.Status status;
}
