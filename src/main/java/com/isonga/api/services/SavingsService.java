package com.isonga.api.services;

import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.models.Savings;
import com.isonga.api.repositories.SavingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SavingsService {

    @Autowired
    private SavingsRepository savingsRepository;

    public Savings save(SavingsRequest request) {
        Savings savings = Savings.builder()
                .userIdNumber(request.getUserIdNumber())
                .amount(request.getAmount()) // Already BigDecimal
                .target(request.getTarget()) // Already BigDecimal
                .dateReceived(LocalDate.now()) // Optional
                .build();

        return savingsRepository.save(savings);
    }

    public List<Savings> findAll() {
        return savingsRepository.findAll();
    }

    public List<Savings> findByUserIdNumber(String idNumber) {
        return savingsRepository.findByUserIdNumber(idNumber);
    }
}
