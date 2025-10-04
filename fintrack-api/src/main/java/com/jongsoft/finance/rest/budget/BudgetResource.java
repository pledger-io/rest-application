package com.jongsoft.finance.rest.budget;

import static com.jongsoft.finance.rest.ApiConstants.TAG_BUDGETS;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.model.BudgetResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Tag(name = TAG_BUDGETS)
@Controller("/api/budgets")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class BudgetResource {

    private final Logger log = LoggerFactory.getLogger(BudgetResource.class);

    private final BudgetProvider budgetProvider;
    private final ExpenseProvider expenseProvider;
    private final FilterFactory filterFactory;

    private final CurrentUserProvider currentUserProvider;
    private final TransactionProvider transactionProvider;

    public BudgetResource(
            BudgetProvider budgetProvider,
            ExpenseProvider expenseProvider,
            FilterFactory filterFactory,
            CurrentUserProvider currentUserProvider,
            TransactionProvider transactionProvider) {
        this.budgetProvider = budgetProvider;
        this.expenseProvider = expenseProvider;
        this.filterFactory = filterFactory;
        this.currentUserProvider = currentUserProvider;
        this.transactionProvider = transactionProvider;
    }

    @Get("/current")
    @Operation(summary = "Current month", description = "Get the budget for the current month.")
    @ApiDefaults
    BudgetResponse currentMonth() {
        return budgetProvider
                .lookup(LocalDate.now().getYear(), LocalDate.now().getMonthValue())
                .map(BudgetResponse::new)
                .getOrThrow(() -> StatusException.notFound("Budget not found for current month."));
    }

    @Get("/{year}/{month}")
    @Operation(
            summary = "Get any month",
            description = "Get the budget for the given year and month combination.")
    @ApiDefaults
    BudgetResponse givenMonth(@PathVariable int year, @PathVariable int month) {
        return budgetProvider
                .lookup(year, month)
                .map(BudgetResponse::new)
                .getOrThrow(() -> StatusException.notFound("Budget not found for month."));
    }

    @Get("/auto-complete{?token}")
    @Operation(
            summary = "Lookup expense",
            description = "Search for expenses that match the provided token",
            parameters =
                    @Parameter(
                            name = "token",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = String.class)))
    List<EntityRef.NamedEntity> autocomplete(@Nullable String token) {
        return expenseProvider
                .lookup(filterFactory.expense().name(token, false))
                .content()
                .toJava();
    }

    @Get
    @Operation(
            summary = "First budget start",
            description =
                    "Computes the date of the start of the first budget registered in FinTrack")
    LocalDate firstBudget() {
        return budgetProvider
                .first()
                .map(Budget::getStart)
                .getOrThrow(() -> StatusException.notFound("No budget found"));
    }

    @Put
    @Operation(
            summary = "Create initial budget",
            description = "Create a new budget in the system.")
    @ApiResponse(
            responseCode = "400",
            content = @Content(schema = @Schema(implementation = JsonError.class)),
            description = "There is already an open budget.")
    @Validated
    @Status(HttpStatus.CREATED)
    void create(@Body BudgetCreateRequest createRequest) {
        var startDate = createRequest.getStart();
        var existing = budgetProvider.lookup(startDate.getYear(), startDate.getMonthValue());
        if (existing.isPresent()) {
            throw StatusException.badRequest(
                    "Cannot start a new budget, there is already a budget open.");
        }

        currentUserProvider.currentUser().createBudget(startDate, createRequest.income());
    }

    @Patch
    @Operation(
            summary = "Patch budget.",
            description = "Update an existing budget that is not yet closed in the system.")
    BudgetResponse patchBudget(@Valid @Body BudgetCreateRequest patchRequest) {
        var startDate = patchRequest.getStart();

        var budget = budgetProvider
                .lookup(startDate.getYear(), startDate.getMonthValue())
                .getOrThrow(() -> StatusException.notFound(
                        "No budget is active yet, create a budget first."));

        budget.indexBudget(startDate, patchRequest.income());
        return budgetProvider
                .lookup(startDate.getYear(), startDate.getMonthValue())
                .map(BudgetResponse::new)
                .getOrThrow(() -> StatusException.internalError(
                        "Could not get budget after updating the period."));
    }

    @Patch("/expenses")
    @Operation(
            summary = "Patch Expenses",
            description = "Create or update an expense in the currents month budget.")
    BudgetResponse patchExpenses(@Valid @Body ExpensePatchRequest patchRequest) {
        var currentDate = LocalDate.now().withDayOfMonth(1);

        var budget = budgetProvider
                .lookup(currentDate.getYear(), currentDate.getMonthValue())
                .getOrThrow(() -> StatusException.notFound(
                        "Cannot update expenses, no budget available" + " yet."));

        if (patchRequest.expenseId() != null) {
            log.debug("Updating expense {} within active budget.", patchRequest.expenseId());

            if (budget.getStart().isBefore(currentDate)) {
                log.info(
                        "Starting new budget period as the current period {} is after the existing"
                                + " start of {}",
                        currentDate,
                        budget.getStart());
                budget.indexBudget(currentDate, budget.getExpectedIncome());
                budget = budgetProvider
                        .lookup(currentDate.getYear(), currentDate.getMonthValue())
                        .getOrThrow(
                                () -> StatusException.internalError("Updating of budget failed."));
            }

            var toUpdate = budget.getExpenses()
                    .first(expense -> Objects.equals(expense.getId(), patchRequest.expenseId()))
                    .getOrThrow(() -> StatusException.badRequest(
                            "Attempted to update a non existing expense."));

            toUpdate.updateExpense(patchRequest.amount());
        } else {
            budget.createExpense(
                    patchRequest.name(), patchRequest.amount() - 0.01, patchRequest.amount());
        }

        return budgetProvider
                .lookup(currentDate.getYear(), currentDate.getMonthValue())
                .map(BudgetResponse::new)
                .getOrThrow(() ->
                        StatusException.internalError("Error whilst fetching updated budget."));
    }

    @Get("/expenses/{id}/{year}/{month}")
    @Operation(
            summary = "Compute expense",
            description = "Computes the expense for the provided year and month")
    List<ComputedExpenseResponse> computeExpense(
            @PathVariable long id, @PathVariable int year, @PathVariable int month) {
        var dateRange = DateUtils.forMonth(year, month);

        return budgetProvider.lookup(year, month).stream()
                .flatMap(budget -> budget.getExpenses().stream())
                .filter(expense -> expense.getId() == id)
                .map(expense -> {
                    var filter = filterFactory
                            .transaction()
                            .ownAccounts()
                            .range(dateRange)
                            .expenses(Collections.List(new EntityRef(expense.getId())));

                    return new ComputedExpenseResponse(
                            expense.computeBudget(),
                            transactionProvider
                                    .balance(filter)
                                    .getOrSupply(() -> BigDecimal.ZERO)
                                    .doubleValue(),
                            dateRange);
                })
                .toList();
    }
}
