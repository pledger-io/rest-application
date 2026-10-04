package com.jongsoft.finance.budget.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.budget.adapter.api.BudgetCalculator;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.BudgetFetcherApi;
import com.jongsoft.finance.rest.model.BudgetResponse;
import com.jongsoft.finance.rest.model.ExpenseComputedResponse;
import com.jongsoft.finance.rest.model.ExpenseResponse;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
class BudgetFetcherController implements BudgetFetcherApi {

    private final Logger logger;
    private final BudgetProvider budgetProvider;
    private final ExpenseProvider expenseProvider;
    private final BudgetCalculator budgetCalculator;
    private final FilterProvider<ExpenseProvider.FilterCommand> filterFactory;

    BudgetFetcherController(
            BudgetProvider budgetProvider,
            ExpenseProvider expenseProvider,
            BudgetCalculator budgetCalculator,
            FilterProvider<ExpenseProvider.FilterCommand> filterFactory) {
        this.budgetProvider = budgetProvider;
        this.expenseProvider = expenseProvider;
        this.budgetCalculator = budgetCalculator;
        this.filterFactory = filterFactory;
        this.logger = LoggerFactory.getLogger(BudgetFetcherController.class);
    }

    @Override
    public List<ExpenseComputedResponse> computeBudgetExpenseBalance(
            Integer year, Integer month, List<Long> expenseId) {
        logger.info("Computing budget expense balance for {}-{}.", year, month);

        return budgetCalculator.computeExpenses(YearMonth.of(year, month)).stream()
                .filter(c ->
                        expenseId.isEmpty() || expenseId.contains(c.expense().getId()))
                .map(c -> new ExpenseComputedResponse(
                        c.expense().getId(),
                        c.amountLeft().doubleValue(),
                        c.dailyLeft().doubleValue(),
                        c.amountSpent().doubleValue(),
                        c.dailySpent().doubleValue()))
                .toList();
    }

    @Override
    public BudgetResponse findByFilter(Integer year, Integer month, Boolean firstOnly) {
        logger.info("Finding budget by year {} and month {}.", year, month);

        if (firstOnly != null && firstOnly) {
            var budget = budgetProvider
                    .first()
                    .getOrThrow(() ->
                            StatusException.badRequest("Cannot fetch budget, no budget found."));
            return BudgetMapper.toBudgetResponse(budget);
        }

        var date = LocalDate.now().withDayOfMonth(1);
        if (year != null) {
            date = date.withYear(year);
        }
        if (month != null) {
            date = date.withMonth(month);
        }

        var budget = budgetProvider
                .lookup(date.getYear(), date.getMonthValue())
                .getOrThrow(() -> StatusException.notFound("Budget not found for the given date."));

        return BudgetMapper.toBudgetResponse(budget);
    }

    @Override
    public List<@Valid ExpenseResponse> findExpensesByFilter(String name) {
        logger.info("Finding expenses by name {}.", name);
        var filter = filterFactory.create().name(name, false);

        return expenseProvider
                .lookup(filter)
                .content()
                .map(BudgetMapper::toBudgetExpense)
                .toJava();
    }
}
