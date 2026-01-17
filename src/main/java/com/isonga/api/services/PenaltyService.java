package com.isonga.api.services;

import com.isonga.api.dto.PenaltyRequest;
import com.isonga.api.models.Activity;
import com.isonga.api.models.Penalty;
import com.isonga.api.repositories.PenaltyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PenaltyService {

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private ActivityService activityService;

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
                .type(Activity.Type.penalty) // Ensure 'penalty' is added to Activity.Type enum
                .description("Penalty Issued: " + request.getReason())
                .amount(request.getAmount().doubleValue())
                .date(LocalDate.now())
                .status(Activity.Status.pending)
                .build());

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

        return updatedPenalty;
    }
    
    @Transactional
    public void deletePenalty(String penaltyId) {
        if (!penaltyRepository.existsById(penaltyId)) {
            throw new IllegalArgumentException("Penalty not found");
        }
        penaltyRepository.deleteById(penaltyId);
    }
}