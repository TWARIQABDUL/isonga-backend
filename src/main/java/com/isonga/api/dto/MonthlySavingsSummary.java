package com.isonga.api.dto;

public record MonthlySavingsSummary(
    String month,
    double amount,
    double target
) {}

