package com.isonga.api.services;

import com.isonga.api.dto.LoanRequest;
import com.isonga.api.models.Loan;
import com.isonga.api.models.Savings;
import com.isonga.api.repositories.LoanRepository;
import com.isonga.api.repositories.SavingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
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
        List<Loan> existingLoans = loanRepository.findByUserIdNumber(userIdNumber);
        boolean hasUnpaidLoan = existingLoans.stream().anyMatch(
                loan -> loan.getStatus() == Loan.Status.pending
                        || loan.getStatus() == Loan.Status.approved
                        || loan.getStatus() == Loan.Status.active
        );
        if (hasUnpaidLoan) {
            throw new IllegalArgumentException(
                    "You already have a pending, approved, or active loan. Complete it before requesting a new one.");
        }

        BigDecimal totalSavings = getTotalSavings(userIdNumber);
        BigDecimal maxLoanAllowed = totalSavings.multiply(MAX_LOAN_PERCENT);

        if (request.getAmount().compareTo(maxLoanAllowed) > 0) {
            throw new IllegalArgumentException("Loan amount exceeds 80% of your total savings.");
        }

        BigDecimal interestRate = FIXED_INTEREST;

        BigDecimal totalWithInterest = request.getAmount().multiply(BigDecimal.ONE.add(interestRate));
        BigDecimal monthlyPayment = totalWithInterest.divide(
                BigDecimal.valueOf(request.getDuration()), 2, RoundingMode.HALF_UP);

        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserIdNumber(userIdNumber);
        loan.setAmount(request.getAmount());
        loan.setPurpose(request.getPurpose());
        loan.setDuration(request.getDuration());
        loan.setInterestRate(interestRate.multiply(BigDecimal.valueOf(100)));
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRequestDate(LocalDate.now());
        loan.setStatus(Loan.Status.pending);

        return loanRepository.save(loan);
    }

    public List<Loan> getLoansForUser(String userIdNumber) {
        return loanRepository.findByUserIdNumber(userIdNumber);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    private BigDecimal getTotalSavings(String userIdNumber) {
        List<Savings> savings = savingsRepository.findByUserIdNumber(userIdNumber);
        return savings.stream()
                .map(Savings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Loan updateLoanStatus(String loanId, Loan.Status status) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        loan.setStatus(status);
        if (status == Loan.Status.approved) {
            loan.setApprovalDate(LocalDate.now());
        }
        return loanRepository.save(loan);
    }

    /**
     * ✅ Auto Reject Loans Not Collected within 1 day after approval
     */
    @Scheduled(cron = "0 0 0 * * *")  // Every midnight
    @Transactional
    public void autoRejectUncollectedLoans() {
        List<Loan> approvedLoans = loanRepository.findAll().stream()
                .filter(loan -> loan.getStatus() == Loan.Status.approved)
                .toList();

        LocalDate today = LocalDate.now();

        for (Loan loan : approvedLoans) {
            if (loan.getApprovalDate() != null && loan.getApprovalDate().plusDays(1).isBefore(today)) {
                loan.setStatus(Loan.Status.rejected);
                loanRepository.save(loan);
                System.out.println("Loan auto-rejected: " + loan.getId());
            }
        }
    }
    public Loan collectLoan(String loanId, String userIdNumber) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    //this will be enforced by the admins in peron for now
    // if (!loan.getUserIdNumber().equals(userIdNumber)) {
    //     throw new IllegalArgumentException("You can only collect your own loan.");
    // }

    if (loan.getStatus() != Loan.Status.approved) {
        throw new IllegalArgumentException("Only approved loans can be collected.");
    }

    loan.setStatus(Loan.Status.active);
    loan.setApprovalDate(LocalDate.now());
    return loanRepository.save(loan);
}

}
