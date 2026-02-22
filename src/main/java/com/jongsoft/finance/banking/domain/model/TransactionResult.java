package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.banking.types.TransactionType;

import java.time.LocalDate;

public record TransactionResult(
        TransactionType type,
        LocalDate date,
        AccountResult from,
        AccountResult to,
        String description,
        double amount) {
    public record AccountResult(long id, String name) {}
}
