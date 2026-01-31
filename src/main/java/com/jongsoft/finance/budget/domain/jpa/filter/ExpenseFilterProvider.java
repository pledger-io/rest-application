package com.jongsoft.finance.budget.domain.jpa.filter;

import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
class ExpenseFilterProvider implements FilterProvider<ExpenseProvider.FilterCommand> {
    @Override
    public ExpenseProvider.FilterCommand create() {
        return new ExpenseFilterCommand();
    }
}
