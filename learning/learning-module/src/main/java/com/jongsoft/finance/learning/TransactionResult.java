package com.jongsoft.finance.learning;

import com.jongsoft.finance.core.TransactionType;

import java.time.LocalDate;

public record TransactionResult(TransactionType type, LocalDate date, String from, String to, String description, double amount) {
}
