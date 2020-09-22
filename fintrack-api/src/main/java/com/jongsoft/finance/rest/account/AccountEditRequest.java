package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Introspected
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AccountEditRequest {

    @NotNull
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @NotBlank
    private String currency;

    @Pattern(regexp = "^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Z0-9]){9,30}$)((?:[ \\-]?[A-Z0-9]{3,5}){2,7})([ \\-]?[A-Z0-9]{1,3})?$")
    private String iban;

    @Pattern(regexp = "^([a-zA-Z]{4}[a-zA-Z]{2}[a-zA-Z0-9]{2}([a-zA-Z0-9]{3})?)$")
    private String bic;
    private String number;

    @Min(-2)
    @Max(2)
    private double interest;
    private Periodicity interestPeriodicity;

    @NotNull
    private String type;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public double getInterest() {
        return interest;
    }

    public Periodicity getInterestPeriodicity() {
        return interestPeriodicity;
    }

    public String getType() {
        return type;
    }
}
