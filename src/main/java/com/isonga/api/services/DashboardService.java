package com.isonga.api.services;

import com.isonga.api.models.User;
import com.isonga.api.repositories.LoanRepository;
import com.isonga.api.repositories.SavingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SavingsRepository savingsRepository;
    private final LoanRepository loanRepository;

    public Map<String, Object> getSummary(User user) {
        double totalSavings = savingsRepository.sumByUserIdNumber(user.getIdNumber());
        double totalLoans = loanRepository.sumByUserIdNumber(user.getIdNumber());
        double monthlyContribution = 1000; // You can later calculate this dynamically
        double availableCredit = totalSavings * 1.5;  // Example logic

        return Map.of(
                "totalSavings", totalSavings,
                "totalLoans", totalLoans,
                "monthlyContribution", monthlyContribution,
                "availableCredit", availableCredit
        );
    }
}
