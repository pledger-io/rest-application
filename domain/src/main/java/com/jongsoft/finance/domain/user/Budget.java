package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.messaging.commands.budget.UpdateExpenseCommand;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import lombok.*;

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
    @ToString(of = "name")
    @EqualsAndHashCode(of = "id")
    public class Expense implements AggregateBase {
        private Long id;
        private final String name;
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

        /**
         * Create an expense and bind it to its parent budget.
         * This will not register the expense in the system yet.
         */
        public Expense(long id, String name, double amount) {
            this.id = id;
            this.name = name;
            this.upperBound = amount;
            this.lowerBound = amount - 0.01;
            expenses = expenses.append(this);
        }

        @BusinessMethod
        public void updateExpense(double expectedExpense) {
            if (computeExpenses() + expectedExpense > expectedIncome) {
                throw StatusException.badRequest(
                        "Expected expenses exceeds the expected income.",
                        "validation.budget.expense.exceeds.income");
            }

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
    }

    private Long id;
    private LocalDate start;
    private LocalDate end;

    @Builder.Default
    private Sequence<Expense> expenses = Collections.List();
    private double expectedIncome;

    private transient boolean active;

    Budget(LocalDate start, double expectedIncome) {
        if (expectedIncome < 1) {
            throw StatusException.badRequest(
                    "Expected income cannot be less than 1.",
                    "validation.budget.income.too.low");
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
            for (var expense : expenses) {
                newBudget.new Expense(
                        expense.id,
                        expense.name,
                        BigDecimal.valueOf(expense.computeBudget())
                                .multiply(deviation)
                                .setScale(0, RoundingMode.CEILING)
                                .doubleValue());
            }
            newBudget.activate();

            return newBudget;
        }

        return this;
    }

    @BusinessMethod
    public void createExpense(String name, double lowerBound, double upperBound) {
        if (end != null) {
            throw StatusException.badRequest(
                    "Cannot add expense to an already closed budget period.",
                    "validation.budget.expense.add.budget.closed");
        }

        if (computeExpenses() + upperBound > expectedIncome) {
            throw StatusException.badRequest(
                    "Expected expenses exceeds the expected income.",
                    "validation.budget.expense.exceeds.income");
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
            throw StatusException.badRequest(
                    "Already closed budget cannot be closed again.",
                    "validation.budget.already.closed");
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
