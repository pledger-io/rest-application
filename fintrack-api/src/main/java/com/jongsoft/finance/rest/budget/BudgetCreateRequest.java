package com.jongsoft.finance.rest.budget;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@Serdeable.Deserializable
class BudgetCreateRequest {
    private int year;
    @Max(12) @Min(1) private int month;
    @Min(0) private double income;

    public BudgetCreateRequest(@Min(1900) int year, int month, double income) {
        this.year = year;
        this.month = month;
        this.income = income;
    }

    public LocalDate getStart() {
        return LocalDate.of(year, month, 1);
    }

    @Deprecated
    public int getYear() {
        return year;
    }

    @Deprecated
    public int getMonth() {
        return month;
    }

    public double getIncome() {
        return income;
    }
}
