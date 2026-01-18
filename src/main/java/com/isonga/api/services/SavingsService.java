package com.isonga.api.services;

import com.isonga.api.dto.MonthlySavingsSummary;
import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.dto.SavingsUpdateDTO;
import com.isonga.api.models.Activity;
import com.isonga.api.models.Loan;
import com.isonga.api.models.Penalty;
import com.isonga.api.models.Savings;
import com.isonga.api.models.User;
import com.isonga.api.repositories.LoanRepository;
import com.isonga.api.repositories.PenaltyRepository;
import com.isonga.api.repositories.SavingsRepository;
import com.isonga.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SavingsService {

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private EmailService emailService;

    // Fixed Fee
    private static final BigDecimal INGOBOKA_FEE = new BigDecimal("200.00");

    @Transactional
    public Savings save(SavingsRequest request) {
        // 1. Validate Amount (Prevent negative savings)
        if (request.getAmount().compareTo(INGOBOKA_FEE) < 0) {
            throw new IllegalArgumentException("Deposit amount must be at least " + INGOBOKA_FEE + " (Ingoboka Fee)");
        }

        LocalDate today = LocalDate.now();
        int weekNum = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int currentYear = today.getYear();

        // 2. Calculate Final Savings (Total - 200)
        BigDecimal finalSavingsAmount = request.getAmount().subtract(INGOBOKA_FEE);

        Savings savings = Savings.builder()
                .userIdNumber(request.getUserIdNumber())
                .amount(finalSavingsAmount) // Net amount
                .ingoboka(INGOBOKA_FEE)     // Fixed fee
                .dateReceived(today)
                .weekNumber(weekNum)
                .year(currentYear)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Log the activity (Log the FULL amount so user sees what they sent)
        activityService.saveActivity(Activity.builder()
                .userIdNumber(request.getUserIdNumber())
                .type(Activity.Type.deposit)
                .description("Monthly savings (Incl. " + INGOBOKA_FEE + " Ingoboka)")
                .amount(request.getAmount().doubleValue()) 
                .date(today)
                .status(Activity.Status.completed)
                .build());

        Savings savedSavings = savingsRepository.save(savings);

        // 4. Send email (Send FULL amount context)
        sendSummaryEmail(request.getUserIdNumber(), request.getAmount());

        return savedSavings;
    }

    private void sendSummaryEmail(String userIdNumber, BigDecimal todayAmount) {
        try {
            User user = userRepository.findByIdNumber(userIdNumber).orElse(null);
            
            if (user != null && user.getEmail() != null) {
                // Active Loans
                List<Loan> loans = loanRepository.findByUserIdNumber(userIdNumber);
                BigDecimal activeLoan = loans.stream()
                        .filter(l -> l.getStatus() == Loan.Status.approved || l.getStatus() == Loan.Status.active)
                        .map(Loan::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Total Savings (DB sum of 'amount' column only - excludes Ingoboka history)
                double dbTotal = savingsRepository.sumByUserIdNumber(userIdNumber);
                BigDecimal totalSavings = BigDecimal.valueOf(dbTotal);
                double ingobokaTotal = savingsRepository.sumIngobokaByUserIdNumber(userIdNumber);

                // Penalties
                List<Penalty> penalties = penaltyRepository.findByUserIdNumber(userIdNumber);

                BigDecimal paidPenalties = penalties.stream()
                        .filter(p -> p.getStatus() == Penalty.Status.PAID)
                        .map(Penalty::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal unpaidPenalties = penalties.stream()
                        .filter(p -> p.getStatus() == Penalty.Status.PENDING) 
                        .map(Penalty::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal availablePenalties = paidPenalties.add(unpaidPenalties);

                emailService.sendSavingsSummary(
                        user.getEmail(),
                        user.getFullName(),
                        todayAmount,
                        activeLoan,
                        availablePenalties,
                        paidPenalties,
                        unpaidPenalties,
                        totalSavings,
                        ingobokaTotal
                );
            }
        } catch (Exception e) {
            System.err.println("Error preparing savings email: " + e.getMessage());
        }
    }

    public List<Savings> findAll() {
        return savingsRepository.findAll();
    }

    public List<Savings> findByUserIdNumber(String idNumber) {
        return savingsRepository.findByUserIdNumber(idNumber);
    }

    // ⚠️ CRITICAL: Ensure SavingsRepository native query aliases 'ingoboka' as 'target'
    // OR update this line to: item.get("ingoboka")
    public List<MonthlySavingsSummary> getMonthlySummary(String userIdNumber) {
        List<Map<String, Object>> results = savingsRepository.findMonthlySavingsSummary(userIdNumber);
        return results.stream()
                .map(item -> new MonthlySavingsSummary(
                        (String) item.get("month"),
                        ((Number) item.get("amount")).doubleValue(),
                        // Ensure repository query says: "SUM(ingoboka) as ingoboka"
                        ((Number) item.get("ingoboka")).doubleValue())) 
                .toList();
    }

    public List<Map<String, Object>> findDailyRepor(){
        return savingsRepository.findDailyReport();
    }

    public List<Map<String, Object>> getSavingsReport(String userIdNumber, Integer year, Integer month, Integer week) {
        return savingsRepository.findSavingsReport(userIdNumber, year, month, week);
    }

    @Transactional
    public Savings updateSavings(Long id, SavingsUpdateDTO request, String adminId) {
        Savings savings = savingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings record found with id: " + id));

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCreation = ChronoUnit.MINUTES.between(savings.getCreatedAt(), now);

        if (minutesSinceCreation > 60) {
            throw new RuntimeException("Update failed: Transaction cannot be edited after 1 hour of creation.");
        }

        savings.setAmount(request.getAmount());
        
        // Ensure SavingsUpdateDTO has 'target' or rename to 'ingoboka'
        savings.setIngoboka(request.getIngoboka()); 

        savings.setEditedBy(adminId);
        savings.setEditedAt(now);

        return savingsRepository.save(savings);
    }
}