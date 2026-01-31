package com.jongsoft.finance.budget.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.budget.domain.commands.CloseBudgetCommand;
import com.jongsoft.finance.budget.domain.commands.CreateBudgetCommand;
import com.jongsoft.finance.budget.domain.commands.CreateExpenseCommand;
import com.jongsoft.finance.budget.domain.commands.UpdateExpenseCommand;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;

import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Introspected
public class Budget {

    @Introspected
    public static class Expense implements Classifier {
        private Long id;
        private final String name;
        private double lowerBound;
        private double upperBound;
        private Budget budget;

        private Expense(String name, double lowerBound, double upperBound) {
            if (lowerBound >= upperBound) {
                throw new IllegalStateException(
                        "Lower bound of expense cannot be higher then upper bound.");
            }

            this.name = name;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        /**
         * Create an expense and bind it to its parent budget. This will not register the expense in
         * the system yet.
         */
        public Expense(long id, String name, double upperBound) {
            this.id = id;
            this.name = name;
            this.upperBound = upperBound;
            this.lowerBound = upperBound - 0.01;
        }

        public void updateExpense(double expectedExpense) {
            if ((budget.computeExpenses() - computeBudget() + expectedExpense)
                    > budget.expectedIncome) {
                throw StatusException.badRequest(
                        "Expected expenses exceeds the expected income.",
                        "validation.budget.expense.exceeds.income");
            }

            lowerBound = expectedExpense - .01;
            upperBound = expectedExpense;

            UpdateExpenseCommand.expenseUpdated(id, BigDecimal.valueOf(expectedExpense));
        }

        public double computeBudget() {
            return BigDecimal.valueOf(lowerBound)
                    .add(BigDecimal.valueOf(upperBound))
                    .divide(BigDecimal.valueOf(2), new MathContext(6, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Expense expense) {
                return Objects.equals(id, expense.id);
            }
            return super.equals(obj);
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getLowerBound() {
            return lowerBound;
        }

        public double getUpperBound() {
            return upperBound;
        }
    }

    private Long id;
    private LocalDate start;
    private LocalDate end;
    private Sequence<Expense> expenses;
    private double expectedIncome;

    private transient boolean active;

    Budget(
            Long id,
            LocalDate start,
            LocalDate end,
            java.util.List<Expense> expenses,
            double expectedIncome) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.expenses = Collections.List(expenses);
        this.expectedIncome = expectedIncome;
        this.active = true;

        this.expenses.forEach(e -> e.budget = this);
    }

    private Budget(LocalDate start, double expectedIncome) {
        if (expectedIncome < 1) {
            throw StatusException.badRequest(
                    "Expected income cannot be less than 1.", "validation.budget.income.too.low");
        }

        this.start = start;
        this.expectedIncome = expectedIncome;
        this.expenses = Collections.List();
    }

    public Budget indexBudget(LocalDate perDate, double expectedIncome) {
        if (!Objects.equals(this.start, perDate)) {
            this.close(perDate);

            var deviation = BigDecimal.ONE.add(BigDecimal.valueOf(expectedIncome)
                    .subtract(BigDecimal.valueOf(this.expectedIncome))
                    .divide(BigDecimal.valueOf(this.expectedIncome), 20, RoundingMode.HALF_UP));

            var newBudget = new Budget(perDate, expectedIncome);
            for (var expense : expenses) {
                newBudget.expenses = newBudget.expenses.append(new Expense(
                        expense.id,
                        expense.name,
                        BigDecimal.valueOf(expense.computeBudget())
                                .multiply(deviation)
                                .setScale(0, RoundingMode.CEILING)
                                .doubleValue()));
            }
            newBudget.activate();

            return newBudget;
        }

        return this;
    }

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
        CreateExpenseCommand.expenseCreated(name, start, BigDecimal.valueOf(upperBound));
    }

    void activate() {
        if (id == null && !active) {
            active = true;
            expenses.forEach(e -> e.budget = this);
            CreateBudgetCommand.budgetCreated(
                    expectedIncome,
                    start,
                    expenses.map(e -> new CreateBudgetCommand.CreateExpense(
                                    e.getId(), e.getUpperBound()))
                            .toJava());
        }
    }

    void close(LocalDate endDate) {
        if (this.end != null) {
            throw StatusException.badRequest(
                    "Already closed budget cannot be closed again.",
                    "validation.budget.already.closed");
        }

        this.end = endDate;
        CloseBudgetCommand.budgetClosed(id, endDate);
    }

    public double computeExpenses() {
        return expenses.stream().mapToDouble(Expense::computeBudget).sum();
    }

    public Expense determineExpense(String name) {
        return expenses.filter(e -> e.getName().equalsIgnoreCase(name))
                .first(t -> true)
                .getOrSupply(() -> null);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public Sequence<Expense> getExpenses() {
        return expenses;
    }

    public double getExpectedIncome() {
        return expectedIncome;
    }

    public boolean isActive() {
        return active;
    }

    public static Budget create(LocalDate start, double expectedIncome) {
        return new Budget(start, expectedIncome);
    }
}
