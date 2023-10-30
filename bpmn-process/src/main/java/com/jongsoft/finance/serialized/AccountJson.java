package com.jongsoft.finance.serialized;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.function.Supplier;

@Getter
@Setter
@Builder
@Serdeable
public class AccountJson implements Serializable {

    private String name;
    private String description;
    private String currency;

    private String icon;

    private double interest;
    private Periodicity periodicity;

    private String iban;
    private String bic;
    private String number;
    private String type;

    public static AccountJson fromDomain(Account account, Supplier<byte[]> iconSupplier) {
        var builder = AccountJson.builder()
                .bic(account.getBic())
                .currency(account.getCurrency())
                .description(account.getDescription())
                .iban(account.getIban())
                .number(account.getNumber())
                .type(account.getType())
                .name(account.getName())
                .periodicity(account.getInterestPeriodicity())
                .interest(account.getInterest());

        if (account.getImageFileToken() != null) {
            builder.icon(Hex.toHexString(iconSupplier.get()));
        }

        return builder.build();
    }
}
