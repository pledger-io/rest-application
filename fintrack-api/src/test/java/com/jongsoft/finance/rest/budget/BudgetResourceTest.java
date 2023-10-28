package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@MicronautTest
@DisplayName("Budget resources")
class BudgetResourceTest extends TestSetup {

    @Inject
    private BudgetProvider budgetProvider;
    @Inject
    private ExpenseProvider expenseProvider;
    @Inject
    private TransactionProvider transactionProvider;

    @Replaces
    @MockBean
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @Replaces
    @MockBean
    ExpenseProvider expenseProvider() {
        return Mockito.mock(ExpenseProvider.class);
    }

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Test
    @DisplayName("Fetch date earliest known budgetFirst")
    void firstBudget(RequestSpecification spec) {
        Mockito.when(budgetProvider.first()).thenReturn(
                Control.Option(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        // @formatter:off

        spec.when()
                .get("/api/budgets")
            .then()
                .statusCode(200);
        // @formatter:on
   }

    @Test
    @DisplayName("Fetch budget for january 2019")
    void budget(RequestSpecification spec) {
        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(
                Control.Option(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        // @formatter:off
        spec.when()
                .get("/api/budgets/{year}/{month}", 2019, 1)
            .then()
                .statusCode(200)
                .body("income", Matchers.equalTo(200.20F))
                .body("period.from", Matchers.equalTo("2018-02-03"))
                .body("expenses", Matchers.hasSize(1))
                .body("expenses[0].id", Matchers.equalTo(1))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].bounds.lower", Matchers.equalTo(20F))
                .body("expenses[0].bounds.upper", Matchers.equalTo(50F));
        // @formatter:on
    }

    @Test
    @DisplayName("Autocomplete expense based upon token")
    void autocomplete(RequestSpecification spec) {
        Mockito.when(expenseProvider.lookup(Mockito.any())).thenReturn(ResultPage.of(
                Budget.Expense.builder()
                        .id(1L)
                        .name("Grocery")
                        .lowerBound(20D)
                        .upperBound(50D)
                        .build()));

        // @formatter:off
        spec.when()
                .get("/api/budgets/auto-complete?token=gro")
            .then()
                .statusCode(200)
                .body("name", Matchers.hasItem("Grocery"))
                .body("bounds.lower", Matchers.hasItem(20F))
                .body("bounds.upper", Matchers.hasItem(50F));
        // @formatter:on

        var mockFilter = filterFactory.expense();
        verify(expenseProvider).lookup(Mockito.any(ExpenseProvider.FilterCommand.class));
        verify(mockFilter).name("gro", false);
    }

    @Test
    @DisplayName("Start the first budget for an account")
    void create(RequestSpecification spec) {
        Mockito.when(budgetProvider.first()).thenReturn(Control.Option());

        // @formatter:off
        spec.when()
                .body(BudgetCreateRequest.builder()
                        .month(2)
                        .year(2019)
                        .income(2300.33D)
                        .build())
                .put("/api/budgets")
            .then()
                .statusCode(200)
                .body("income", Matchers.equalTo(2300.33F))
                .body("period.from", Matchers.equalTo("2019-02-01"));
        // @formatter:on
    }

    @Test
    @DisplayName("Attempt to index a budget that does not exist")
    void index_noBudget(RequestSpecification spec) {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option());

        // @formatter:off
        spec.when()
                .body(BudgetCreateRequest.builder()
                        .month(2)
                        .year(2019)
                        .income(2300.33D)
                        .build())
                .post("/api/budgets")
            .then()
                .statusCode(404);
        // @formatter:on
    }


    @Test
    @DisplayName("Index an existing budget")
    void index(RequestSpecification spec) {
        var existingBudget = Mockito.spy(Budget.builder()
                .start(LocalDate.of(2018, 1, 1))
                .expectedIncome(2200)
                .id(1L)
                .expenses(Collections.List(Budget.Expense.builder()
                        .lowerBound(22)
                        .upperBound(44)
                        .build())).build());

        Mockito.when(budgetProvider.lookup(2019, 2))
                .thenReturn(Control.Option(existingBudget));

        // @formatter:off
        spec.when()
                .body(BudgetCreateRequest.builder()
                        .month(2)
                        .year(2019)
                        .income(2300.33D)
                        .build())
                .post("/api/budgets")
            .then()
                .statusCode(200)
                .body("income", Matchers.equalTo(2300.33F))
                .body("period.from", Matchers.equalTo("2019-02-01"));
        // @formatter:on

        verify(existingBudget).indexBudget(LocalDate.of(2019, 2, 1), 2300.33D);
    }

    @Test
    @DisplayName("Create a new expense on an existing budget")
    void createExpense(RequestSpecification spec) {
        var builder = Budget.builder()
                .expectedIncome(200.20D)
                .start(LocalDate.of(2018, 2, 3))
                .expenses(Collections.List(Budget.Expense.builder()
                        .id(1L)
                        .name("Grocery")
                        .lowerBound(20D)
                        .upperBound(50D)
                        .build()));
        var budget = Mockito.spy(builder.build());

        Mockito.when(budgetProvider.first()).thenReturn(Control.Option(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option(budget))
                .thenReturn(Control.Option(builder.build()));

        // @formatter:off
        spec.when()
                .body(ExpenseCreateRequest.builder()
                        .name("My new expense")
                        .lowerBound(20)
                        .upperBound(40)
                        .build())
                .put("/api/budgets/expenses")
            .then()
                .statusCode(200)
                .body("expenses[0].id", Matchers.equalTo(1))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].bounds.lower", Matchers.equalTo(20F))
                .body("expenses[0].bounds.upper", Matchers.equalTo(50F));
        // @formatter:on

        verify(budget).createExpense("My new expense", 20, 40);
    }

    @Test
    @DisplayName("Attempt to create a new expense on a budget that does not exist")
    void createExpense_noBudget(RequestSpecification spec) {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option());

        // @formatter:off
        spec.when()
                .body(ExpenseCreateRequest.builder()
                        .name("My new expense")
                        .lowerBound(20)
                        .upperBound(40)
                        .build())
                .put("/api/budgets/expenses")
            .then()
                .statusCode(404);
        // @formatter:on
    }

    @Test
    @DisplayName("Compute the daily expenses for a budget")
    void computeExpense(RequestSpecification spec) {
        Mockito.when(budgetProvider.first()).thenReturn(Control.Option(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
                Control.Option(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option(BigDecimal.valueOf(200)));

        // @formatter:off
        spec.when()
                .get("/api/budgets/expenses/{id}/{year}/{month}", 1, 2019, 1)
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].dailySpent", Matchers.equalTo(6.45F))
                .body("[0].left", Matchers.equalTo(-165F))
                .body("[0].dailyLeft", Matchers.equalTo(-5.32F));
        // @formatter:on
    }
}
