package com.jongsoft.finance.budget.domain.service;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider.FilterCommand;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.budget.domain.model.ComputedExpense;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.time.Range;

import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

@Singleton
class BudgetExpenseCalculator {

    private final BudgetProvider budgetProvider;
    private final TransactionProvider transactionProvider;
    private final FilterProvider<FilterCommand> filterProvider;

    BudgetExpenseCalculator(
            BudgetProvider budgetProvider,
            TransactionProvider transactionProvider,
            FilterProvider<FilterCommand> filterProvider) {
        this.budgetProvider = budgetProvider;
        this.transactionProvider = transactionProvider;
        this.filterProvider = filterProvider;
    }

    public List<ComputedExpense> computeExpenses(YearMonth yearMonth) {
        var dateRange = Dates.range(
                LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1), ChronoUnit.MONTHS);

        return budgetProvider.lookup(yearMonth.getYear(), yearMonth.getMonthValue()).stream()
                .flatMap(b -> b.getExpenses().stream())
                .map(computeForExpense(dateRange))
                .toList();
    }

    private Function<Budget.Expense, ComputedExpense> computeForExpense(
            Range<LocalDate> dateRange) {
        int days = (int) ChronoUnit.DAYS.between(dateRange.from(), dateRange.until());

        return expense -> {
            FilterCommand filter = filterProvider
                    .create()
                    .range(dateRange)
                    .onlyIncome(false)
                    .ownAccounts()
                    .expenses(Collections.List(new EntityRef(expense.getId())));

            BigDecimal balance =
                    transactionProvider.balance(filter).getOrSupply(() -> BigDecimal.ZERO);
            BigDecimal amountLeft = BigDecimal.valueOf(expense.computeBudget()).subtract(balance);
            BigDecimal dailyLeft = calculateDaily(amountLeft, days);
            BigDecimal dailySpent = calculateDaily(balance, days);

            return new ComputedExpense(expense, amountLeft, balance, dailyLeft, dailySpent);
        };
    }

    private BigDecimal calculateDaily(BigDecimal value, int days) {
        return value.divide(BigDecimal.valueOf(days), new MathContext(6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
