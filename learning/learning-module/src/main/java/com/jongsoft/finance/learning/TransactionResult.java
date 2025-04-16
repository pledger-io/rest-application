package com.jongsoft.finance.learning;

import com.jongsoft.finance.core.TransactionType;

import java.time.LocalDate;

public record TransactionResult(TransactionType type, LocalDate date, AccountResult from, AccountResult to, String description, double amount) {
    public record AccountResult(long id, String name) {}
}
