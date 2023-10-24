package com.jongsoft.finance.rest.budget;

import com.jongsoft.lang.time.Range;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode
@Serdeable.Serializable
class ComputedExpenseResponse {

    @EqualsAndHashCode
    @AllArgsConstructor
    public static class DateRange {

        private Range<LocalDate> range;

        public LocalDate getStart() {
            return range.from();
        }

        public LocalDate getEnd() {
            return range.until();
        }
    }

    private double allowed;
    private double spent;
    private DateRange dateRange;

    public ComputedExpenseResponse(double allowed, double spent, Range<LocalDate> dateRange) {
        this.allowed = allowed;
        this.spent = spent;
        this.dateRange = new DateRange(dateRange);
    }

    public double getDailySpent() {
        var days = (int) ChronoUnit.DAYS.between(dateRange.getStart(), dateRange.getEnd());
        return calculateDaily(spent, days).doubleValue();
    }

    public double getLeft() {
        return BigDecimal.valueOf(allowed).subtract(BigDecimal.valueOf(Math.abs(spent))).doubleValue();
    }

    public double getDailyLeft() {
        var days = (int) ChronoUnit.DAYS.between(dateRange.getStart(), dateRange.getEnd());
        return calculateDaily(
                BigDecimal.valueOf(allowed)
                        .subtract(BigDecimal.valueOf(Math.abs(spent)))
                        .doubleValue(),
                days).doubleValue();
    }

    private BigDecimal calculateDaily(double spent, int days) {
        return BigDecimal.valueOf(spent)
                .divide(BigDecimal.valueOf(days), new MathContext(6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
