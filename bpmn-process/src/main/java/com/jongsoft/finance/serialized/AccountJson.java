package com.jongsoft.finance.serialized;

import java.io.Serializable;

import com.jongsoft.finance.domain.account.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountJson implements Serializable {

    private String name;
    private String description;
    private String currency;

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
                .build();
    }
}
