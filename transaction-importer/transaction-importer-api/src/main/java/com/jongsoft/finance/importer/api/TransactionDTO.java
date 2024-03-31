package com.jongsoft.finance.importer.api;

import com.jongsoft.finance.core.TransactionType;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable
public record TransactionDTO(
        // The amount of the transaction
        double amount,
        // The type of the transaction
        TransactionType type,
        // The description of the transaction
        String description,
        // The date of the transaction
        LocalDate transactionDate,
        // The date the transaction starts to accrue interest
        LocalDate interestDate,
        // The date the transaction was booked
        LocalDate bookDate,
        // The IBAN of the opposing account
        String opposingIBAN,
        // The name of the opposing account
        String opposingName) {

    @Override
    public String toString() {
        return "Transfer of " + amount + " to " + opposingName + " (" + opposingIBAN + ") on " + transactionDate;
    }
}
