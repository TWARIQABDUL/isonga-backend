package com.isonga.api.services;

import com.isonga.api.dto.MonthlySavingsSummary;
import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.dto.SavingsUpdateDTO;
import com.isonga.api.models.Activity;
import com.isonga.api.models.Loan;
import com.isonga.api.models.Savings;
import com.isonga.api.models.User;
import com.isonga.api.repositories.LoanRepository;
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
    
    // ✅ Added these repositories to fetch user and loan info
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Savings save(SavingsRequest request) {
        // 1. Capture the current date and time
        LocalDate today = LocalDate.now();
        
        // 2. Calculate the Week Number and Year
        int weekNum = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int currentYear = today.getYear();

        Savings savings = Savings.builder()
                .userIdNumber(request.getUserIdNumber())
                .amount(request.getAmount())
                .target(request.getTarget())
                .dateReceived(today)
                .weekNumber(weekNum)
                .year(currentYear)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Log the activity
        activityService.saveActivity(Activity.builder()
                .userIdNumber(request.getUserIdNumber())
                .type(Activity.Type.deposit)
                .description("Monthly savings contribution")
                .amount(request.getAmount().doubleValue())
                .date(today)
                .status(Activity.Status.completed)
                .build());

        Savings savedSavings = savingsRepository.save(savings);

        // 4. ✅ Trigger the email with calculated data
        sendSummaryEmail(request.getUserIdNumber(), request.getAmount());

        return savedSavings;
    }

    private void sendSummaryEmail(String userIdNumber, BigDecimal todayAmount) {
        try {
            // A. Fetch User to get Email and Name
            User user = userRepository.findByIdNumber(userIdNumber).orElse(null);
            
            if (user != null && user.getEmail() != null) {
                // B. Calculate Active Loans (sum of approved or active)
                List<Loan> loans = loanRepository.findByUserIdNumber(userIdNumber);
                BigDecimal activeLoan = loans.stream()
                        .filter(l -> l.getStatus() == Loan.Status.approved || l.getStatus() == Loan.Status.active)
                        .map(Loan::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // C. Calculate Total Savings (Previous Total + Today's Amount)
                // Note: sumByUserIdNumber likely returns a double, so we convert it.
                double dbTotal = savingsRepository.sumByUserIdNumber(userIdNumber);
                BigDecimal totalSavings = BigDecimal.valueOf(dbTotal);

                // D. Penalties (Set to 0.00 for now)
                BigDecimal availablePenalties = BigDecimal.ZERO;
                BigDecimal paidPenalties = BigDecimal.ZERO;
                BigDecimal unpaidPenalties = BigDecimal.ZERO;

                // E. Call EmailService with ALL required arguments
                emailService.sendSavingsSummary(
                        user.getEmail(),
                        user.getFullName(),
                        todayAmount,
                        activeLoan,
                        availablePenalties,
                        paidPenalties,
                        unpaidPenalties,
                        totalSavings
                );
            }
        } catch (Exception e) {
            System.err.println("Error preparing savings email: " + e.getMessage());
            // We catch errors here so the savings transaction doesn't fail if email fails
        }
    }

    public List<Savings> findAll() {
        return savingsRepository.findAll();
    }

    public List<Savings> findByUserIdNumber(String idNumber) {
        return savingsRepository.findByUserIdNumber(idNumber);
    }

    public List<MonthlySavingsSummary> getMonthlySummary(String userIdNumber) {
        List<Map<String, Object>> results = savingsRepository.findMonthlySavingsSummary(userIdNumber);
        return results.stream()
                .map(item -> new MonthlySavingsSummary(
                        (String) item.get("month"),
                        ((Number) item.get("amount")).doubleValue(),
                        ((Number) item.get("target")).doubleValue()))
                .toList();
    }

    public List<Map<String, Object>> findDailyRepor(){
        List<Map<String, Object>> results = savingsRepository.findDailyReport();
        return results;
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
        savings.setTarget(request.getTarget());
        savings.setEditedBy(adminId);
        savings.setEditedAt(now);

        return savingsRepository.save(savings);
    }
}