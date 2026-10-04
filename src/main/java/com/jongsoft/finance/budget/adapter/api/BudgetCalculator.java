package com.jongsoft.finance.budget.adapter.api;

import com.jongsoft.finance.budget.domain.model.ComputedExpense;

import java.time.YearMonth;
import java.util.List;

public interface BudgetCalculator {

    List<ComputedExpense> computeExpenses(YearMonth yearMonth);
}
