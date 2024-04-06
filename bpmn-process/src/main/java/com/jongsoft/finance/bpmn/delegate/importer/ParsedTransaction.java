package com.jongsoft.finance.bpmn.delegate.importer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.serialized.AccountJson;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@Serdeable
@Deprecated
@ToString(of = {"transactionDate", "opposingName"})
public class ParsedTransaction implements Serializable {

    private double amount;
    private Transaction.Type type;
    private String description;
    private LocalDate transactionDate;
    private LocalDate interestDate;
    private LocalDate bookDate;
    private String opposingIBAN;
    private String opposingName;

    @JsonIgnore
    public AccountJson getAccount() {
        return AccountJson.builder()
                .name(opposingName)
                .iban(opposingIBAN)
                .type(type == Transaction.Type.CREDIT ? "creditor" : "debtor")
                .currency("EUR")// todo this needs to be fixed later on
                .build();
    }
}
