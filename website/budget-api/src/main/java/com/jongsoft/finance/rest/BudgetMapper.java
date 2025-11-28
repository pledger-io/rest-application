package com.jongsoft.finance.rest;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.rest.model.budget.BudgetResponse;
import com.jongsoft.finance.rest.model.budget.DateRange;
import com.jongsoft.finance.rest.model.budget.ExpenseResponse;

import java.math.BigDecimal;

public interface BudgetMapper {

    static BudgetResponse toBudgetResponse(Budget budget) {
        return new BudgetResponse(
                BigDecimal.valueOf(budget.getExpectedIncome()),
                new DateRange(budget.getStart(), budget.getEnd()),
                budget.getExpenses().map(BudgetMapper::toBudgetExpense).toJava());
    }

    static ExpenseResponse toBudgetExpense(Budget.Expense expense) {
        return new ExpenseResponse(
                expense.getId(), expense.getName(), BigDecimal.valueOf(expense.computeBudget()));
    }

    static ExpenseResponse toBudgetExpense(EntityRef.NamedEntity expense) {
        return new ExpenseResponse(expense.getId(), expense.name(), BigDecimal.ZERO);
    }
}
