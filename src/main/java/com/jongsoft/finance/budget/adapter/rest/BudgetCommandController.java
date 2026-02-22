package com.jongsoft.finance.budget.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.rest.BudgetCommandApi;
import com.jongsoft.finance.rest.model.BudgetRequest;
import com.jongsoft.finance.rest.model.BudgetResponse;
import com.jongsoft.finance.rest.model.ExpenseRequest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Objects;

@Controller
class BudgetCommandController implements BudgetCommandApi {

    private final Logger logger;
    private final BudgetProvider budgetProvider;

    BudgetCommandController(BudgetProvider budgetProvider) {
        this.budgetProvider = budgetProvider;
        this.logger = LoggerFactory.getLogger(BudgetCommandController.class);
    }

    @Override
    public HttpResponse<@Valid BudgetResponse> createInitialBudget(BudgetRequest budgetRequest) {
        logger.info(
                "Creating initial budget for year {} and month {}.",
                budgetRequest.getYear(),
                budgetRequest.getMonth());

        budgetProvider
                .lookup(budgetRequest.getYear(), budgetRequest.getMonth())
                .ifPresent(() -> StatusException.badRequest(
                        "A budget already exists, cannot start a new one."));

        Budget.create(
                LocalDate.of(budgetRequest.getYear(), budgetRequest.getMonth(), 1),
                budgetRequest.getIncome());

        var budget = budgetProvider
                .lookup(budgetRequest.getYear(), budgetRequest.getMonth())
                .getOrThrow(() -> StatusException.internalError("Error whilst creating budget."));
        return HttpResponse.created(BudgetMapper.toBudgetResponse(budget));
    }

    @Override
    public BudgetResponse updateCurrentBudget(BudgetRequest budgetRequest) {
        logger.info(
                "Updating budget for year {} and month {}.",
                budgetRequest.getYear(),
                budgetRequest.getMonth());

        var budget = budgetProvider
                .lookup(budgetRequest.getYear(), budgetRequest.getMonth())
                .getOrThrow(() -> StatusException.notFound(
                        "Cannot update budget, no previous version found."));

        budget.indexBudget(budget.getStart(), budgetRequest.getIncome());
        var updateBudget = budgetProvider
                .lookup(budget.getStart().getYear(), budget.getStart().getMonthValue())
                .getOrThrow(() -> StatusException.internalError("Error whilst updating budget."));
        return BudgetMapper.toBudgetResponse(updateBudget);
    }

    @Override
    public BudgetResponse updateExpense(ExpenseRequest expenseRequest) {
        logger.info("Updating expense {}.", expenseRequest.getId());

        var now = LocalDate.now().withDayOfMonth(1);
        var budget = budgetProvider
                .lookup(now.getYear(), now.getMonthValue())
                .getOrThrow(() -> StatusException.notFound(
                        "Cannot update expenses, no budget available yet."));

        if (expenseRequest.getId() != null) {
            logger.debug("Updating expense {} within active budget.", expenseRequest.getId());
            if (budget.getStart().isBefore(now)) {
                logger.debug(
                        "Starting new budget period as the current period {} is after the existing start of {}.",
                        now,
                        budget.getStart());
                budget.indexBudget(now, budget.getExpectedIncome());
                budget = budgetProvider
                        .lookup(now.getYear(), now.getMonthValue())
                        .getOrThrow(() ->
                                StatusException.internalError("Error whilst updating budget."));
            }

            budget.getExpenses()
                    .first(expense -> Objects.equals(expense.getId(), expenseRequest.getId()))
                    .getOrThrow(() -> StatusException.badRequest(
                            "Attempted to update a non existing expense."))
                    .updateExpense(expenseRequest.getAmount());
        } else {
            logger.debug("Creating new expense within active budget.");
            budget.createExpense(
                    expenseRequest.getName(),
                    expenseRequest.getAmount() - 0.01,
                    expenseRequest.getAmount());
        }

        var updateBudget = budgetProvider
                .lookup(budget.getStart().getYear(), budget.getStart().getMonthValue())
                .getOrThrow(() -> StatusException.internalError("Error whilst updating budget."));
        return BudgetMapper.toBudgetResponse(updateBudget);
    }
}
