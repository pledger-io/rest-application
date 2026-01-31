package com.jongsoft.finance.budget.adapter.api;

import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface BudgetProvider {

    Sequence<Budget> lookup();

    Optional<Budget> lookup(int year, int month);

    Optional<Budget> first();
}
