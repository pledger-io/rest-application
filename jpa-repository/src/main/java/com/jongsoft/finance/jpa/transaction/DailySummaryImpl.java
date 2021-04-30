package com.jongsoft.finance.jpa.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.jongsoft.finance.providers.TransactionProvider;

public class DailySummaryImpl implements TransactionProvider.DailySummary {
    private LocalDate day;
    private BigDecimal summary;

    public DailySummaryImpl(LocalDate day, BigDecimal summary) {
        this.day = day;
        this.summary = summary;
    }

    @Override
    public LocalDate day() {
        return day;
    }

    @Override
    public double summary() {
        return summary.doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TransactionProvider.DailySummary other) {
            return summary.compareTo(BigDecimal.valueOf(other.summary())) == 0 &&
                    day.equals(other.day());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, summary);
    }

    @Override
    public String toString() {
        return "DailySummaryImpl{" +
                "day=" + day +
                ", summary=" + summary +
                '}';
    }
}
