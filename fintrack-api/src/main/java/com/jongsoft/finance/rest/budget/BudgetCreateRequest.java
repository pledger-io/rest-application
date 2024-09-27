package com.jongsoft.finance.rest.budget;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@Serdeable.Deserializable
record BudgetCreateRequest (
        @Min(1900) int year,
        @Max(12) @Min(1) int month,
        @Min(0) double income
) {

    public LocalDate getStart() {
        return LocalDate.of(year, month, 1);
    }

}
