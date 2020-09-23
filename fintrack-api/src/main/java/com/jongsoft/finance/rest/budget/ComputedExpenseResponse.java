package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.core.date.DateRange;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

class ComputedExpenseResponse {

    private double allowed;
    private double spent;
    private DateRange dateRange;

    public ComputedExpenseResponse(double allowed, double spent, DateRange dateRange) {
        this.allowed = allowed;
        this.spent = spent;
        this.dateRange = dateRange;
    }

    public double getDailySpent() {
        return calculateDaily(spent, dateRange.amountOfDays() + 1).doubleValue();
    }

    public double getLeft() {
        return BigDecimal.valueOf(allowed).subtract(BigDecimal.valueOf(Math.abs(spent))).doubleValue();
    }

    public double getDailyLeft() {
        return calculateDaily(BigDecimal.valueOf(allowed).subtract(BigDecimal.valueOf(Math.abs(spent))).doubleValue(),
                dateRange.amountOfDays() + 1).doubleValue();
    }

    private BigDecimal calculateDaily(double spent, int days) {
        return BigDecimal.valueOf(spent)
                .divide(BigDecimal.valueOf(days), new MathContext(6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
