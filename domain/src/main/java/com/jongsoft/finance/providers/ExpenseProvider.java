package com.jongsoft.finance.providers;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Budget;

public interface ExpenseProvider extends DataProvider<Budget.Expense> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
    }

    ResultPage<Budget.Expense> lookup(FilterCommand filter);

    default boolean supports(Class<Budget.Expense> supportingClass) {
        return Budget.Expense.class.equals(supportingClass);
    }
}
