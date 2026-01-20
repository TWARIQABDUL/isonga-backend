package com.isonga.api.services;

import com.isonga.api.models.User;
import com.isonga.api.repositories.LoanRepository;
import com.isonga.api.repositories.PenaltyRepository;
import com.isonga.api.repositories.SavingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SavingsRepository savingsRepository;
    private final LoanRepository loanRepository;
    private final PenaltyRepository penaltyRepository;
    private final com.isonga.api.repositories.UserRepository userRepository;

    public Map<String, Object> getSummary(User user) {
        double totalSavings = savingsRepository.sumByUserIdNumber(user.getIdNumber());
        double totalLoans = loanRepository.sumByUserIdNumber(user.getIdNumber());
        double totalIngoboka = savingsRepository.sumUserIngobokaAmount(user.getIdNumber());
        double totalIbihano = penaltyRepository.sumByUserIdNumber(user.getIdNumber());

        double monthlyContribution = savingsRepository.monthlyContribution(user.getIdNumber());
        double availableCredit = totalSavings * 1.5;  // Example logic

        return Map.of(
                "totalSavings", totalSavings,
                "totalLoans", totalLoans,
                "totalIngoboka", totalIngoboka,
                "totalIbihano", totalIbihano,
                "monthlyContribution", monthlyContribution,
                "availableCredit", availableCredit
        );
    }

    public Map<String, Object> getAdminSummary() {
        long totalUsers = userRepository.count();
        double totalSavings = savingsRepository.sumTotalAmount();
        double totalLoans = loanRepository.sumTotalAmount();
        double totalIngoboka = savingsRepository.sumIngobokaAmount();

        return Map.of(
                "totalUsers", totalUsers,
                "totalSavings", totalSavings,
                "totalLoans", totalLoans,
                "totalIngoboka", totalIngoboka
        );
    }
}
