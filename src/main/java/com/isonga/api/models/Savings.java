package com.isonga.api.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_number", nullable = false, length = 50)
    private String userIdNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal target;

    @Column(name = "date_received", insertable = false, updatable = false)
    private LocalDate dateReceived;

    @Column(name = "week_number", insertable = false, updatable = false)
    private Integer weekNumber;

    @Column(insertable = false, updatable = false)
    private Integer year;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
