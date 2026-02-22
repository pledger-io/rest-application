package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.banking.types.FailureCode;
import com.jongsoft.finance.banking.types.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionCommand(
        LocalDate date,
        String description,
        TransactionType type,
        FailureCode failureCode,
        String currency,
        long fromAccount,
        long toAccount,
        BigDecimal amount)
        implements ApplicationEvent {

    public static void transactionCreated(
            LocalDate date,
            String description,
            TransactionType type,
            FailureCode failureCode,
            String currency,
            long fromAccount,
            long toAccount,
            BigDecimal amount) {
        new CreateTransactionCommand(
                        date,
                        description,
                        type,
                        failureCode,
                        currency,
                        fromAccount,
                        toAccount,
                        amount)
                .publish();
    }
}
