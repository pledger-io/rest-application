package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.model.budget.BudgetResponse;
import com.jongsoft.finance.rest.model.budget.ComputedExpenseResponse;
import com.jongsoft.finance.rest.model.budget.ExpenseResponse;
import com.jongsoft.lang.Collections;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
class BudgetFetcherController implements BudgetFetcherApi {

    private final Logger logger;
    private final BudgetProvider budgetProvider;
    private final ExpenseProvider expenseProvider;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    BudgetFetcherController(
            BudgetProvider budgetProvider,
            ExpenseProvider expenseProvider,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider) {
        this.budgetProvider = budgetProvider;
        this.expenseProvider = expenseProvider;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.logger = LoggerFactory.getLogger(BudgetFetcherController.class);
    }

    @Override
    public List<ComputedExpenseResponse> computeBudgetExpenseBalance(
            Integer year, Integer month, List<Long> expenseId) {
        logger.info("Computing budget expense balance for {}-{}.", year, month);

        var budget = budgetProvider
                .lookup(year, month)
                .getOrThrow(() ->
                        StatusException.badRequest("Cannot fetch expenses, no budget found."));

        var dateRange = DateUtils.forMonth(year, month);
        var days = (int) ChronoUnit.DAYS.between(dateRange.from(), dateRange.until());

        var computedExpenses = new ArrayList<ComputedExpenseResponse>();
        for (var expense : budget.getExpenses()) {
            if (!expenseId.isEmpty() && !expenseId.contains(expense.getId())) {
                continue;
            }

            var filter = filterFactory
                    .transaction()
                    .range(dateRange)
                    .onlyIncome(false)
                    .ownAccounts()
                    .expenses(Collections.List(new EntityRef(expense.getId())));
            var balance = transactionProvider
                    .balance(filter)
                    .getOrSupply(() -> BigDecimal.ZERO)
                    .doubleValue();
            computedExpenses.add(new ComputedExpenseResponse(
                    expense.getId(),
                    expense.computeBudget() - balance,
                    calculateDaily(
                                    BigDecimal.valueOf(expense.computeBudget())
                                            .subtract(BigDecimal.valueOf(Math.abs(balance)))
                                            .doubleValue(),
                                    days)
                            .doubleValue(),
                    balance,
                    calculateDaily(balance, days).doubleValue()));
        }

        return computedExpenses;
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
        var filter = filterFactory.expense().name(name, false);

        return expenseProvider
                .lookup(filter)
                .content()
                .map(BudgetMapper::toBudgetExpense)
                .toJava();
    }

    private BigDecimal calculateDaily(double spent, int days) {
        return BigDecimal.valueOf(spent)
                .divide(BigDecimal.valueOf(days), new MathContext(6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
