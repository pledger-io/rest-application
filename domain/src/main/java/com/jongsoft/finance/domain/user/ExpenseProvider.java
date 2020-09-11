package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.domain.core.ResultPage;

public interface ExpenseProvider extends DataProvider<Budget.Expense> {

    interface FilterCommand {
        FilterCommand name(String value, boolean exact);
    }

    ResultPage<Budget.Expense> lookup(FilterCommand filter);

    default boolean supports(Class<Budget.Expense> supportingClass) {
        return Budget.Expense.class.equals(supportingClass);
    }
}
