package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.transaction.Transaction;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Getter
@Serdeable
@AllArgsConstructor
public class TransactionJson implements Serializable {

    private final String fromAccount;
    private final String toAccount;

    private final String description;
    private final String currency;
    private final double amount;

    private final LocalDate date;
    private final LocalDate interestDate;
    private final LocalDate bookDate;

    public static TransactionJson fromDomain(Transaction transaction) {
        return TransactionJson.builder()
                .fromAccount(transaction.computeFrom().getName())
                .toAccount(transaction.computeTo().getName())
                .description(transaction.getDescription())
                .currency(transaction.getCurrency())
                .amount(transaction.computeAmount(transaction.computeFrom()))
                .date(transaction.getDate())
                .interestDate(transaction.getInterestDate())
                .bookDate(transaction.getBookDate())
                .build();
    }
}
