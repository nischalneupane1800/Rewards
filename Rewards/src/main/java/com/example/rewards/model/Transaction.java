package com.example.rewards.model;

import java.time.LocalDate;

public record Transaction(
        String id,
        String customerId,
        double amount,
        LocalDate date
) {}