package com.isonga.api.services;

import com.isonga.api.dto.LoanRequest;
import com.isonga.api.models.Loan;
import com.isonga.api.models.Savings;
import com.isonga.api.repositories.LoanRepository;
import com.isonga.api.repositories.SavingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
// import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final SavingsRepository savingsRepository;

    private final BigDecimal MAX_LOAN_PERCENT = new BigDecimal("0.80");
    private final BigDecimal FIXED_INTEREST = new BigDecimal("0.05");

    @Transactional
    public Loan requestLoan(String userIdNumber, LoanRequest request) {
        // 🚫 Check if user already has a non-completed loan
        List<Loan> existingLoans = loanRepository.findByUserIdNumber(userIdNumber);
        boolean hasUnpaidLoan = existingLoans.stream().anyMatch(
                loan -> loan.getStatus() != Loan.Status.completed);
        if (hasUnpaidLoan) {
            throw new IllegalArgumentException(
                    "You already have a pending, approved, or active loan. Complete it before requesting a new one.");
        }

        BigDecimal totalSavings = getTotalSavings(userIdNumber);
        BigDecimal maxLoanAllowed = totalSavings.multiply(MAX_LOAN_PERCENT);

        if (request.getAmount().compareTo(maxLoanAllowed) > 0) {
            throw new IllegalArgumentException("Loan amount exceeds 80% of your total savings.");
        }

        // ✅ Use fixed interest rate
        BigDecimal interestRate = FIXED_INTEREST;

        // ✅ Calculate monthly payment
        BigDecimal totalWithInterest = request.getAmount().multiply(BigDecimal.ONE.add(interestRate));
        BigDecimal monthlyPayment = totalWithInterest.divide(
                BigDecimal.valueOf(request.getDuration()), 2, RoundingMode.HALF_UP);

        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserIdNumber(userIdNumber);
        loan.setAmount(request.getAmount());
        loan.setPurpose(request.getPurpose());
        loan.setDuration(request.getDuration());
        loan.setInterestRate(interestRate.multiply(BigDecimal.valueOf(100))); // 5.00%
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRequestDate(LocalDate.now());
        loan.setStatus(Loan.Status.pending);

        return loanRepository.save(loan);
    }

    public List<Loan> getLoansForUser(String userIdNumber) {
        return loanRepository.findByUserIdNumber(userIdNumber);
    }

    private BigDecimal getTotalSavings(String userIdNumber) {
        List<Savings> savings = savingsRepository.findByUserIdNumber(userIdNumber);
        return savings.stream()
                .map(Savings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

}
