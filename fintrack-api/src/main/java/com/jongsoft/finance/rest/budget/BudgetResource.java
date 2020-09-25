package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.finance.rest.model.BudgetResponse;
import com.jongsoft.finance.rest.model.ExpenseResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactivestreams.Publisher;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Budget")
@Controller("/api/budgets")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class BudgetResource {

    private final CurrentUserProvider currentUserProvider;
    private final BudgetProvider budgetProvider;
    private final ExpenseProvider expenseProvider;
    private final FilterFactory filterFactory;

    private final TransactionProvider transactionProvider;

    public BudgetResource(
            CurrentUserProvider currentUserProvider,
            BudgetProvider budgetProvider,
            ExpenseProvider expenseProvider,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider) {
        this.currentUserProvider = currentUserProvider;
        this.budgetProvider = budgetProvider;
        this.expenseProvider = expenseProvider;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
    }

    @Get
    @Operation(
            summary = "First budget start",
            description = "Computes the date of the start of the first budget registered in FinTrack"
    )
    LocalDate firstBudget() {
        return budgetProvider.first()
                .map(Budget::getStart)
                .getOrSupply(() -> null);
    }

    @Get("/{year}/{month}")
    @Operation(
            summary = "Get budget",
            description = "Lookup the active budget during the provided year and month",
            parameters = {
                    @Parameter(name = "year", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class, description = "The year")),
                    @Parameter(name = "month", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class, description = "The month"))
            }
    )
    Single<BudgetResponse> budget(@PathVariable int year, @PathVariable int month) {
        return budgetProvider.lookup(year, month)
                .map(BudgetResponse::new);
    }

    @Get("/auto-complete")
    @Operation(
            summary = "Lookup expense",
            description = "Search in FinTrack for expenses that match the provided token",
            parameters = @Parameter(name = "token", in = ParameterIn.QUERY, schema = @Schema(implementation = String.class))
    )
    List<ExpenseResponse> autocomplete(@QueryValue String token) {
        return expenseProvider.lookup(
                filterFactory.expense()
                        .name(token, false))
                .content()
                .map(ExpenseResponse::new)
                .toJava();
    }

    @Put
    @Operation(
            summary = "Create budget",
            description = "Create a new budget in the system with the provided start date"
    )
    Single<BudgetResponse> create(@Valid @Body BudgetCreateRequest budgetCreateRequest) {
        LocalDate startDate = LocalDate.of(budgetCreateRequest.getYear(), budgetCreateRequest.getMonth(), 1);

        return Single.create(
                emitter -> {
                    var budget = currentUserProvider.currentUser()
                            .createBudget(startDate, budgetCreateRequest.getIncome());
                    emitter.onSuccess(new BudgetResponse(budget));
                });
    }

    @Post
    @Operation(
            summary = "Index budget",
            description = "Indexing a budget will change it expenses and expected income by a percentage"
    )
    Single<BudgetResponse> index(@Valid @Body BudgetCreateRequest budgetUpdateRequest) {
        var startDate = LocalDate.of(budgetUpdateRequest.getYear(), budgetUpdateRequest.getMonth(), 1);

        return budgetProvider.lookup(budgetUpdateRequest.getYear(), budgetUpdateRequest.getMonth())
                .map(budget -> budget.indexBudget(startDate, budgetUpdateRequest.getIncome()))
                .map(BudgetResponse::new);
    }

    @Put("/expenses")
    @Operation(
            summary = "Create expense",
            description = "Add a new expense to all existing budgets"
    )
    Single<BudgetResponse> createExpense(@Valid @Body ExpenseCreateRequest createRequest) {
        var now = LocalDate.now();

        return budgetProvider.lookup(now.getYear(), now.getMonthValue())
                .map(budget -> {
                    budget.createExpense(createRequest.getName(), createRequest.getLowerBound(), createRequest.getUpperBound());
                    return budget;
                })
                .map(BudgetResponse::new);
    }

    @Get("/expenses/{id}/{year}/{month}")
    @Operation(
            summary = "Compute expense",
            description = "Computes the expense for the provided year and month"
    )
    Publisher<ComputedExpenseResponse> computeExpense(@PathVariable long id, @PathVariable int year, @PathVariable int month) {
        var dateRange = DateRange.forMonth(year, month);

        return budgetProvider.lookup(year, month)
                .flatMapPublisher(budget -> Flowable.fromIterable(budget.getExpenses()))
                .filter(expense -> expense.getId() == id)
                .map(expense -> {
                    var filter = filterFactory.transaction()
                            .ownAccounts()
                            .range(dateRange)
                            .expenses(API.List(new EntityRef(expense.getId())));

                    return new ComputedExpenseResponse(
                            expense.computeBudget(),
                            transactionProvider.balance(filter).getOrSupply(() -> 0D),
                            dateRange
                    );
                });
    }
}
