package com.jongsoft.finance.budget.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;
import java.util.List;

public record CreateBudgetCommand(
        double expectedIncome, LocalDate start, List<CreateExpense> expenses)
        implements ApplicationEvent {

    public record CreateExpense(long expenseId, double expected) {}

    public static void budgetCreated(
            double expectedIncome, LocalDate start, List<CreateExpense> expenses) {
        new CreateBudgetCommand(expectedIncome, start, expenses).publish();
    }
}
