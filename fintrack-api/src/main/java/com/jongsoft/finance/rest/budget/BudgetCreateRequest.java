package com.jongsoft.finance.rest.budget;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Introspected
@NoArgsConstructor
class BudgetCreateRequest {

    @Min(1900)
    private int year;

    @Max(12)
    @Min(1)
    private int month;

    @Min(0)
    private double income;

    @Builder
    BudgetCreateRequest(int year, int month, double income) {
        this.year = year;
        this.month = month;
        this.income = income;
    }

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
