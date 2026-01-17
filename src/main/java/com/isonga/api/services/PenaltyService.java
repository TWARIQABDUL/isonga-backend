package com.isonga.api.services;

import com.isonga.api.dto.PenaltyRequest;
import com.isonga.api.models.Activity;
import com.isonga.api.models.Penalty;
import com.isonga.api.models.User;
import com.isonga.api.repositories.PenaltyRepository;
import com.isonga.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PenaltyService {

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private ActivityService activityService;

    // ✅ ADDED: Dependencies for Email
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Penalty createPenalty(PenaltyRequest request) {
        Penalty penalty = Penalty.builder()
                .userIdNumber(request.getUserIdNumber())
                .amount(request.getAmount())
                .reason(request.getReason())
                .dateIssued(LocalDateTime.now())
                .status(Penalty.Status.PENDING)
                .build();

        Penalty savedPenalty = penaltyRepository.save(penalty);

        // Log the activity
        activityService.saveActivity(Activity.builder()
                .userIdNumber(request.getUserIdNumber())
                .type(Activity.Type.penalty) 
                .description("Penalty Issued: " + request.getReason())
                .amount(request.getAmount().doubleValue())
                .date(LocalDate.now())
                .status(Activity.Status.pending)
                .build());

        // ✅ TRIGGER EMAIL: Penalty Issued
        sendPenaltyEmail(request.getUserIdNumber(), request.getAmount(), request.getReason(), "ISSUED");

        return savedPenalty;
    }

    public List<Penalty> getAllPenalties() {
        return penaltyRepository.findAll();
    }

    public List<Penalty> getUserPenalties(String userIdNumber) {
        return penaltyRepository.findByUserIdNumber(userIdNumber);
    }

    @Transactional
    public Penalty markAsPaid(String penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new IllegalArgumentException("Penalty not found"));

        if (penalty.getStatus() == Penalty.Status.PAID) {
            throw new IllegalArgumentException("Penalty is already paid");
        }

        penalty.setStatus(Penalty.Status.PAID);
        Penalty updatedPenalty = penaltyRepository.save(penalty);

        // Log Activity
        activityService.saveActivity(Activity.builder()
                .userIdNumber(penalty.getUserIdNumber())
                .type(Activity.Type.penalty)
                .description("Penalty Paid: " + penalty.getReason())
                .amount(penalty.getAmount().doubleValue())
                .date(LocalDate.now())
                .status(Activity.Status.completed)
                .build());

        // ✅ TRIGGER EMAIL: Penalty Paid
        sendPenaltyEmail(penalty.getUserIdNumber(), penalty.getAmount(), penalty.getReason(), "PAID");

        return updatedPenalty;
    }
    
    @Transactional
    public void deletePenalty(String penaltyId) {
        if (!penaltyRepository.existsById(penaltyId)) {
            throw new IllegalArgumentException("Penalty not found");
        }
        penaltyRepository.deleteById(penaltyId);
    }

    // ✅ HELPER METHOD for Sending Emails safely
    private void sendPenaltyEmail(String userIdNumber, BigDecimal amount, String reason, String status) {
        try {
            User user = userRepository.findByIdNumber(userIdNumber).orElse(null);
            if (user != null && user.getEmail() != null) {
                emailService.sendPenaltyNotification(
                        user.getEmail(),
                        user.getFullName(),
                        amount,
                        reason,
                        status
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send penalty email: " + e.getMessage());
            // We catch exception so the transaction doesn't fail if email service is down
        }
    }
}