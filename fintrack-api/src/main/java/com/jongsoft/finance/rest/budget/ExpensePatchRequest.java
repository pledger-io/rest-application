package com.jongsoft.finance.rest.budget;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;

@Serdeable
public record ExpensePatchRequest(
        Long expenseId,
        String name,
        @Min(0)
        double amount) {
}
