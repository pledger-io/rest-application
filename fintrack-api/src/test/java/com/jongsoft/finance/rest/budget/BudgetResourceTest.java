package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.model.BudgetResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;

class BudgetResourceTest extends TestSetup {

    private BudgetResource subject;

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private BudgetProvider budgetProvider;
    @Mock
    private ExpenseProvider expenseProvider;
    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private FilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject = new BudgetResource(
                currentUserProvider,
                budgetProvider,
                expenseProvider,
                filterFactory,
                transactionProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        new EventBus(applicationEventPublisher);
    }

    @Test
    void firstBudget() {
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

        Assertions.assertThat(subject.firstBudget())
                .isNotNull()
                .isEqualTo("2018-02-03");
    }

    @Test
    void budget() {
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

        Assertions.assertThat(subject.budget(2019, 1))
                .isNotNull()
                .isInstanceOf(BudgetResponse.class)
                .hasFieldOrPropertyWithValue("income", 200.20D)
                .hasFieldOrPropertyWithValue("period.from", LocalDate.of(2018, 2, 3))
                .satisfies(response -> {
                    Assertions.assertThat(response.getExpenses())
                            .hasSize(1)
                            .first()
                            .hasFieldOrPropertyWithValue("id", 1L)
                            .hasFieldOrPropertyWithValue("name", "Grocery")
                            .hasFieldOrPropertyWithValue("bounds.lower", 20D)
                            .hasFieldOrPropertyWithValue("bounds.upper", 50D);
                });
    }

    @Test
    void autocomplete() {
        Mockito.when(expenseProvider.lookup(Mockito.any())).thenReturn(ResultPage.of(
                Budget.Expense.builder()
                        .id(1L)
                        .name("Grocery")
                        .lowerBound(20D)
                        .upperBound(50D)
                        .build()));

        var response = subject.autocomplete("gro");

        var mockFilter = filterFactory.expense();
        verify(expenseProvider).lookup(Mockito.any(ExpenseProvider.FilterCommand.class));
        verify(mockFilter).name("gro", false);
    }

    @Test
    void create() {
        Mockito.when(budgetProvider.first()).thenReturn(Control.Option());

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        Assertions.assertThat(subject.create(request))
                .isNotNull()
                .isInstanceOf(BudgetResponse.class)
                .hasFieldOrPropertyWithValue("income", 2300.33D)
                .hasFieldOrPropertyWithValue("period.from", LocalDate.of(2019, 2, 1));

        verify(applicationEventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
    }

    @Test
    void index_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option());

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        Assertions.assertThatThrownBy(() -> subject.index(request))
                .isInstanceOf(StatusException.class)
                .hasMessage("No budget found");
    }


    @Test
    void index() {
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

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        Assertions.assertThat(subject.index(request))
                .isNotNull()
                .isInstanceOf(BudgetResponse.class)
                .hasFieldOrPropertyWithValue("income", 2300.33D)
                .hasFieldOrPropertyWithValue("period.from", LocalDate.of(2019, 2, 1));

        verify(existingBudget).indexBudget(LocalDate.of(2019, 2, 1), 2300.33D);
    }

    @Test
    void createExpense() {
        Budget budget = Mockito.spy(Budget.builder()
                .expectedIncome(200.20D)
                .start(LocalDate.of(2018, 2, 3))
                .expenses(Collections.List(Budget.Expense.builder()
                        .id(1L)
                        .name("Grocery")
                        .lowerBound(20D)
                        .upperBound(50D)
                        .build()))
                .build());

        Mockito.when(budgetProvider.first()).thenReturn(Control.Option(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option(budget));

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        Assertions.assertThat(subject.createExpense(request))
                .satisfies(response -> {
                    Assertions.assertThat(response.getExpenses())
                            .hasSize(2)
                            .first()
                            .hasFieldOrPropertyWithValue("id", 1L)
                            .hasFieldOrPropertyWithValue("name", "Grocery")
                            .hasFieldOrPropertyWithValue("bounds.lower", 20D)
                            .hasFieldOrPropertyWithValue("bounds.upper", 50D);
                });

        verify(budget).createExpense("My new expense", 20, 40);
    }

    @Test
    void createExpense_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Control.Option());

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        Assertions.assertThatThrownBy(() -> subject.createExpense(request))
                .isInstanceOf(StatusException.class)
                .hasMessage("No budget found");
    }

    @Test
    void computeExpense() {
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

        Assertions.assertThat(subject.computeExpense(1, 2019, 1))
                .isNotNull()
                .first()
                .isInstanceOf(ComputedExpenseResponse.class)
                .satisfies(response -> {
                    Assertions.assertThat(response.getDailySpent()).isEqualTo(6.45D);
                    Assertions.assertThat(response.getLeft()).isEqualTo(-165D);
                    Assertions.assertThat(response.getDailyLeft()).isEqualTo(-5.32D);
                });
    }
}
