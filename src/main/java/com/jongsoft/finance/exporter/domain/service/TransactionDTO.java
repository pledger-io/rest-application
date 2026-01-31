package com.jongsoft.finance.exporter.domain.service;

import com.jongsoft.finance.banking.types.TransactionType;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;
import java.util.List;

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
        String opposingName,
        // Optional: The name of the budget the transaction falls under
        String budget,
        // Optional: The category of the transaction
        String category,
        // Optional: The tags of the transaction
        List<String> tags) {

    @Override
    public String toString() {
        return "Transfer of "
                + amount
                + " to "
                + opposingName
                + " ("
                + opposingIBAN
                + ") on "
                + transactionDate;
    }
}
