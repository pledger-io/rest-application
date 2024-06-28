package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.transaction.Transaction;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@Serdeable
@JsonSchema(title = "Transaction", description = "Transaction details", uri = "/transaction")
public class TransactionJson implements Serializable {

    /**
     * The account from which the transaction was made.
     */
    @NonNull
    private final String fromAccount;
    /**
     * The account to which the transaction was made.
     */
    @NonNull
    private final String toAccount;

    @NonNull
    private final String description;
    @NonNull
    private final String currency;
    private final double amount;

    @NonNull
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
