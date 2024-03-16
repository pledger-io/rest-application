package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.apache.commons.lang3.mutable.MutableLong;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Budget resource")
public class BudgetResourceTest extends TestSetup {

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

    private List<Budget> knownBudgets;

    @BeforeEach
    void setup() {
        knownBudgets = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            var date = LocalDate.of(invocation.getArgument(0), invocation.getArgument(1, Integer.class), 1);
            return Control.Option(knownBudgets.stream()
                    .filter(budget -> budget.getStart().isBefore(date) || budget.getStart().isEqual(date))
                    .max(Comparator.comparing(Budget::getStart))
                    .orElse(null));
        }).when(budgetProvider).lookup(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @DisplayName("First budget date when a budget exists")
    void firstBudgetDate(RequestSpecification spec) {
        var budget = createBudget();
        Mockito.when(budgetProvider.first()).thenReturn(
                Control.Option(budget));

        // @formatter:off
        var body = spec.given()
                .contentType("application/json")
            .when()
                .get("/api/budgets")
            .then()
                .statusCode(HttpStatus.OK.getCode())
                .extract()
                .asString();
        // @formatter:on

        Assertions.assertThat(body)
                .as("The first date of a budget")
                .isEqualTo("\"2018-02-01\"");
    }

    @Test
    @DisplayName("First budget without any budget existing")
    void firstBudget_notFound(RequestSpecification spec) {
        when(budgetProvider.first()).thenReturn(Control.Option());

        // @formatter:off
        spec.when()
                .get("/api/budgets")
            .then()
                .statusCode(HttpStatus.NOT_FOUND.getCode());
        // @formatter:on
    }

    @Test
    @DisplayName("Get the budget for the current month")
    void currentMonth(RequestSpecification spec) {
        knownBudgets.add(createBudget());

        // @formatter:off
        spec.when()
                .get("/api/budgets/current")
            .then()
                .statusCode(200)
                .body("income", Matchers.equalTo(200.20F))
                .body("period.from", Matchers.equalTo("2018-02-01"))
                .body("expenses", Matchers.hasSize(1))
                .body("expenses[0].id", Matchers.equalTo(1))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].expected", Matchers.equalTo(40F));
        // @formatter:on
    }

    @Test
    @DisplayName("Get the budget for the year 2019 and month Feb")
    void givenMonth(RequestSpecification spec) {
        knownBudgets.add(createBudget());

        // @formatter:off
        spec.when()
                .get("/api/budgets/{year}/{month}", 2019, 2)
                .then()
                .statusCode(200)
                .body("income", Matchers.equalTo(200.20F))
                .body("period.from", Matchers.equalTo("2018-02-01"))
                .body("expenses", Matchers.hasSize(1))
                .body("expenses[0].id", Matchers.equalTo(1))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].expected", Matchers.equalTo(40F));
        // @formatter:on
    }

    @Test
    @DisplayName("Autocomplete expense based upon token")
    void autocomplete(RequestSpecification spec) {
        Mockito.when(expenseProvider.lookup(Mockito.any())).thenReturn(ResultPage.of(
                new EntityRef.NamedEntity(1, "Groceries")));

        // @formatter:off
        spec.when()
                .get("/api/budgets/auto-complete?token=gro")
            .then()
                .statusCode(200)
                .body("name", Matchers.hasItem("Groceries"));
        // @formatter:on

        var mockFilter = filterFactory.expense();
        verify(expenseProvider).lookup(Mockito.any(ExpenseProvider.FilterCommand.class));
        verify(mockFilter).name("gro", false);
    }

    @Test
    @DisplayName("Start the first budget for an account")
    void create(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .body("""
                        {
                            "month": 2,
                            "year": 2019,
                            "income": 2300.33
                        }
                        """)
                .put("/api/budgets")
            .then()
                .statusCode(201);
        // @formatter:on

        Mockito.verify(ACTIVE_USER).createBudget(LocalDate.of(2019, 2, 1), 2300.33);
    }

    @Test
    @DisplayName("Start the first budget, but one already exists")
    void create_alreadyPresent(RequestSpecification spec) {
        knownBudgets.add(createBudget());

        // @formatter:off
        spec.when()
                .body("""
                        {
                            "month": 2,
                            "year": 2019,
                            "income": 2300.33
                        }
                        """)
                .put("/api/budgets")
            .then()
                .statusCode(400)
                .body("message", Matchers.equalTo("Cannot start a new budget, there is already a budget open."));
        // @formatter:on
    }

    @Test
    @DisplayName("Patch the budget, but there is no active budget")
    void patchBudget_noActiveBudget(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .body("""
                        {
                            "month": 2,
                            "year": 2019,
                            "income": 2300.33
                        }
                        """)
                .patch("/api/budgets")
            .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("No budget is active yet, create a budget first."));
        // @formatter:on
    }

    @Test
    @DisplayName("Patch the budget")
    void patchBudget(RequestSpecification spec) {
        var budget = createBudget();
        knownBudgets.add(budget);

        // @formatter:off
        spec.when()
                .body("""
                        {
                            "month": 2,
                            "year": 2019,
                            "income": 3500.00
                        }
                        """)
                .patch("/api/budgets")
            .then()
                .statusCode(200)
                .body("period.from", Matchers.equalTo("2019-02-01"))
                .body("income", Matchers.equalTo(3500F))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].expected", Matchers.equalTo(700F));
        // @formatter:on

        Mockito.verify(budget).indexBudget(LocalDate.of(2019, 2, 1), 3500D);
    }

    @Test
    @DisplayName("Create a new expense in an existing budget")
    void createNewExpense(RequestSpecification spec) {
        knownBudgets.add(createBudget());

        // @formatter:off
        spec.when()
                .body(new ExpensePatchRequest(null, "Car", 10))
                .patch("/api/budgets/expenses")
            .then()
                .statusCode(200)
                .body("period.from", Matchers.equalTo("2018-02-01"))
                .body("income", Matchers.equalTo(200.2F))
                .body("expenses[1].name", Matchers.equalTo("Grocery"))
                .body("expenses[1].expected", Matchers.equalTo(40F))
                .body("expenses[0].name", Matchers.equalTo("Car"))
                .body("expenses[0].expected", Matchers.equalTo(10F));
        // @formatter:on
    }

    @Test
    @DisplayName("Update expense forcing new budget period")
    void updateExistingExpense(RequestSpecification spec) {
        var now = LocalDate.now();
        knownBudgets.add(createBudget());

        // @formatter:off
        spec.when()
                .body(new ExpensePatchRequest(1L, "Grocery", 40))
                .patch("/api/budgets/expenses")
            .then()
                .statusCode(200)
                .body("period.from", Matchers.equalTo(now.withDayOfMonth(1).toString()))
                .body("income", Matchers.equalTo(200.2F))
                .body("expenses[0].name", Matchers.equalTo("Grocery"))
                .body("expenses[0].expected", Matchers.equalTo(40F));
        // @formatter:on
    }

    @Test
    @DisplayName("Compute the daily expenses for a budget")
    void computeExpense(RequestSpecification spec) {
        knownBudgets.add(createBudget());
        Mockito.when(transactionProvider.balance(Mockito.any()))
                .thenReturn(Control.Option(BigDecimal.valueOf(200)));

        // @formatter:off
        spec.when()
                .get("/api/budgets/expenses/{id}/{year}/{month}", 1, 2019, 1)
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].dailySpent", Matchers.equalTo(6.45F))
                .body("[0].left", Matchers.equalTo(-160F))
                .body("[0].dailyLeft", Matchers.equalTo(-5.16F));
        // @formatter:on
    }

    private Budget createBudget() {
        var budget = Mockito.spy(Budget.builder()
                .id(1L)
                .expectedIncome(200.20D)
                .start(LocalDate.of(2018, 2, 1))
                .build());
        budget.new Expense(1, "Grocery", 40);

        var mutableId = new MutableLong(10);
        Mockito.doAnswer(invocation -> {
            invocation.callRealMethod();
            var found = budget.getExpenses()
                    .first(expense -> expense.getId() == null)
                    .get();
            ReflectionUtils.setField(Budget.Expense.class, "id", found, mutableId.incrementAndGet());
            return null;
        }).when(budget).createExpense(Mockito.any(), Mockito.anyDouble(), Mockito.anyDouble());

        Mockito.doAnswer(invocationOnMock -> {
            var updatedBudget = (Budget) invocationOnMock.callRealMethod();
            knownBudgets.add(updatedBudget);
            return updatedBudget;
        }).when(budget).indexBudget(Mockito.any(LocalDate.class), Mockito.anyDouble());

        return budget;
    }
}