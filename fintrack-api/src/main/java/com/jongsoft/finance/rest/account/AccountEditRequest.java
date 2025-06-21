package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Serdeable
record AccountEditRequest(
    @NotNull @NotBlank @Schema(description = "Account name") String name,
    @Schema(description = "Account description") String description,
    @NotNull @NotBlank @Schema(description = "Account currency, must exist in the system")
        String currency,
    @Pattern(
            regexp = "^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Z0-9]){9,30}$)((?:["
                + " \\-]?[A-Z0-9]{3,5}){2,7})([ \\-]?[A-Z0-9]{1,3})?$")
        @Schema(description = "IBAN number")
        String iban,
    @Pattern(regexp = "^([a-zA-Z]{4}[a-zA-Z]{2}[a-zA-Z0-9]{2}([a-zA-Z0-9]{3})?)$")
        @Schema(description = "The banks BIC number")
        String bic,
    @Schema(description = "The account number, in case IBAN is not applicable") String number,
    @Min(-2) @Max(2) double interest,
    Periodicity interestPeriodicity,
    @NotNull String type) {}
