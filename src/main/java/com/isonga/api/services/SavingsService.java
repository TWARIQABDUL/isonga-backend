package com.isonga.api.services;

import com.isonga.api.dto.MonthlySavingsSummary;
import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.dto.SavingsUpdateDTO;
import com.isonga.api.models.Activity;
import com.isonga.api.models.Savings;
import com.isonga.api.repositories.SavingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class SavingsService {

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private ActivityService activityService;

    public Savings save(SavingsRequest request) {
        // 1. Capture the current date and time
        LocalDate today = LocalDate.now();
        
        // 2. Calculate the Week Number and Year manually
        int weekNum = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int currentYear = today.getYear();

        Savings savings = Savings.builder()
                .userIdNumber(request.getUserIdNumber())
                .amount(request.getAmount())
                .target(request.getTarget())
                .dateReceived(today)             // ✅ Set Date
                .weekNumber(weekNum)             // ✅ Set Calculated Week Number
                .year(currentYear)               // ✅ Set Calculated Year
                .createdAt(LocalDateTime.now())  // ✅ Set Timestamp
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

        return savingsRepository.save(savings);
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
        System.out.println("the repot "+results);
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
