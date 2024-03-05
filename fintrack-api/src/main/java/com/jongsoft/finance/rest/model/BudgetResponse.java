package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.Budget;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Stream;

@Serdeable
public class BudgetResponse {

    private final Budget wrapped;

    public BudgetResponse(Budget wrapped) {
        this.wrapped = wrapped;
    }

    public double getIncome() {
        return wrapped.getExpectedIncome();
    }

    public Period getPeriod() {
        return new Period();
    }

    public Stream<ExpenseResponse> getExpenses() {
        return wrapped.getExpenses()
                .map(ExpenseResponse::new)
                .stream()
                .sorted(Comparator.comparing(ExpenseResponse::getName));
    }

    @Serdeable
    public class Period {

        public LocalDate getFrom() {
            return wrapped.getStart();
        }

        public LocalDate getUntil() {
            return wrapped.getEnd();
        }

    }
}
