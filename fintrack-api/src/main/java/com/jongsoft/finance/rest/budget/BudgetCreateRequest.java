package com.jongsoft.finance.rest.budget;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
@Serdeable.Deserializable
class BudgetCreateRequest {

    @Min(1900)
    private int year;

    @Max(12)
    @Min(1)
    private int month;

    @Min(0)
    private double income;

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public double getIncome() {
        return income;
    }
}
