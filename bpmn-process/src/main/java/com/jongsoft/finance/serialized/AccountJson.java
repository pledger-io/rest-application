package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Periodicity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountJson implements Serializable {

    private String name;
    private String description;
    private String currency;

    private double interest;
    private Periodicity periodicity;

    private String iban;
    private String bic;
    private String number;
    private String type;

    public static AccountJson fromDomain(Account account) {
        return AccountJson.builder()
                .bic(account.getBic())
                .currency(account.getCurrency())
                .description(account.getDescription())
                .iban(account.getIban())
                .number(account.getNumber())
                .type(account.getType())
                .name(account.getName())
                .periodicity(account.getInterestPeriodicity())
                .interest(account.getInterest())
                .build();
    }
}
