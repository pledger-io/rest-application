package com.jongsoft.finance.jpa.transaction.entity;

import java.time.LocalDate;
import java.util.Objects;

import com.jongsoft.finance.domain.transaction.TransactionProvider;

public class DailySummaryImpl implements TransactionProvider.DailySummary {
    private LocalDate day;
    private double summary;

    public DailySummaryImpl(LocalDate day, double summary) {
        this.day = day;
        this.summary = summary;
    }

    @Override
    public LocalDate day() {
        return day;
    }

    @Override
    public double summary() {
        return summary;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TransactionProvider.DailySummary other) {
            return Double.compare(other.summary(), summary) == 0 &&
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
