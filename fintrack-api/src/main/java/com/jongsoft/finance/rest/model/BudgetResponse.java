package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.Budget;

import java.time.LocalDate;
import java.util.stream.Stream;

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
                .stream();
    }

    public class Period {

        public LocalDate getFrom() {
            return wrapped.getStart();
        }

        public LocalDate getUntil() {
            return wrapped.getEnd();
        }

    }
}
