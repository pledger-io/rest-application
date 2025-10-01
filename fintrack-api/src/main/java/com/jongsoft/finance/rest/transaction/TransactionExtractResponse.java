package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.learning.TransactionResult;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable
public record TransactionExtractResponse(
        TransactionType type,
        LocalDate date,
        AccountRef from,
        AccountRef to,
        String description,
        double amount) {

    @Serdeable
    public record AccountRef(long id, String name) {}

    public static TransactionExtractResponse from(TransactionResult transactionResult) {
        return new TransactionExtractResponse(
                transactionResult.type(),
                transactionResult.date(),
                new AccountRef(transactionResult.from().id(), transactionResult.from().name()),
                new AccountRef(transactionResult.to().id(), transactionResult.to().name()),
                transactionResult.description(),
                transactionResult.amount());
    }
}
