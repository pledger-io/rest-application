package com.jongsoft.finance.rest.account;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Serdeable.Deserializable
record AccountTransactionCreateRequest (
        @NotNull
        LocalDate date,
        LocalDate interestDate,
        LocalDate bookDate,

        @NotNull
        @NotBlank
        String currency,

        @NotBlank
        @Size(max = 1024)
        String description,

        @NotNull
        double amount,

        @NotNull
        EntityRef source,
        @NotNull
        EntityRef destination,

        EntityRef category,
        EntityRef budget,
        EntityRef contract,
        List<String> tags) {

    @Serdeable.Deserializable
    record EntityRef(
            @NotNull
            Long id,
            String name) {
    }
}
