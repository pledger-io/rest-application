package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.messaging.commands.budget.UpdateExpenseCommand;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
public class Budget implements AggregateBase {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Expense implements AggregateBase {
        private Long id;
        private String name;
        private double lowerBound;
        private double upperBound;

        Expense(String name, double lowerBound, double upperBound) {
            if (lowerBound >= upperBound) {
                throw new IllegalStateException("Lower bound of expense cannot be higher then upper bound.");
            }

            this.name = name;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        Expense indexExpense(BigDecimal deviation) {
            return Expense.builder()
                    .id(id)
                    .name(name)
                    .lowerBound(BigDecimal.valueOf(lowerBound)
                            .multiply(deviation)
                            .setScale(0, RoundingMode.CEILING)
                            .doubleValue())
                    .upperBound(BigDecimal.valueOf(upperBound)
                            .multiply(deviation)
                            .setScale(0, RoundingMode.CEILING)
                            .doubleValue())
                    .build();
        }

        @BusinessMethod
        public void updateExpense(double expectedExpense) {
            lowerBound = expectedExpense - .01;
            upperBound = expectedExpense;

            EventBus.getBus()
                    .send(new UpdateExpenseCommand(
                            id,
                            BigDecimal.valueOf(expectedExpense)));
        }

        public double computeBudget() {
            return BigDecimal.valueOf(lowerBound)
                    .add(BigDecimal.valueOf(upperBound))
                    .divide(BigDecimal.valueOf(2), new MathContext(6, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Expense other) {
                return other.getId().equals(getId());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return 7 + id.hashCode();
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private Long id;
    private LocalDate start;
    private LocalDate end;

    private Sequence<Expense> expenses;
    private double expectedIncome;

    private transient boolean active;

    Budget(LocalDate start, double expectedIncome) {
        if (expectedIncome < 1) {
            throw new IllegalStateException("Expected income cannot be less than 1.");
        }

        this.start = start;
        this.expectedIncome = expectedIncome;
        this.expenses = Collections.List();
    }

    @BusinessMethod
    public Budget indexBudget(LocalDate perDate, double expectedIncome) {
        if (!Objects.equals(this.start, perDate)) {
            this.close(perDate);

            var deviation = BigDecimal.ONE
                    .add(BigDecimal.valueOf(expectedIncome)
                            .subtract(BigDecimal.valueOf(this.expectedIncome))
                            .divide(BigDecimal.valueOf(this.expectedIncome), 20, RoundingMode.HALF_UP));

            var newBudget = new Budget(perDate, expectedIncome);
            newBudget.expenses = expenses.map(e -> e.indexExpense(deviation));
            newBudget.activate();

            return newBudget;
        }

        return this;
    }

    @BusinessMethod
    public void createExpense(String name, double lowerBound, double upperBound) {
        if (end != null) {
            throw new IllegalStateException("Cannot add expense to an already closed budget period.");
        }

        if (computeExpenses() + upperBound > expectedIncome) {
            throw new IllegalStateException("Expected expenses exceeds the expected income.");
        }

        expenses = expenses.append(new Expense(name, lowerBound, upperBound));
        EventBus.getBus().send(new CreateExpenseCommand(name, start, BigDecimal.valueOf(upperBound)));
    }

    void activate() {
        if (id == null && !active) {
            active = true;
            EventBus.getBus().send(new CreateBudgetCommand(this));
        }
    }

    void close(LocalDate endDate) {
        if (this.end != null) {
            throw new IllegalStateException("Already closed budget cannot be closed again.");
        }

        this.end = endDate;
        EventBus.getBus().send(new CloseBudgetCommand(id, endDate));
    }

    public double computeExpenses() {
        return expenses.stream()
                .mapToDouble(Expense::computeBudget)
                .sum();
    }

    public Expense determineExpense(String name) {
        return expenses
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .first(t -> true)
                .getOrSupply(() -> null);
    }
}
