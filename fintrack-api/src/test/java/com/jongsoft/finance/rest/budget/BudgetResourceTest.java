package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                Mono.just(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        StepVerifier.create(subject.firstBudget())
                .assertNext(response -> assertThat(response).isEqualTo("2018-02-03"))
                .verifyComplete();
    }

    @Test
    void budget() {
        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(
                Mono.just(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        StepVerifier.create(subject.budget(2019, 1))
                .assertNext(response -> {
                    assertThat(response.getIncome()).isEqualTo(200.20D);
                    assertThat(response.getPeriod().getFrom()).isEqualTo(LocalDate.of(2018, 2, 3));
                    assertThat(response.getExpenses()).hasSize(1);
                })
                .verifyComplete();
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
        Mockito.when(budgetProvider.first()).thenReturn(Mono.empty());

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        StepVerifier.create(subject.create(request))
                .assertNext(response -> {
                    assertThat(response.getPeriod().getFrom()).isEqualTo(LocalDate.of(2019, 2, 1));
                })
                .verifyComplete();

        verify(applicationEventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
    }

    @Test
    void index_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Mono.error(StatusException.notFound("No such element")));

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        StepVerifier.create(subject.index(request))
                .expectError(StatusException.class)
                .verify();
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
                .thenReturn(Mono.just(existingBudget));

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        StepVerifier.create(subject.index(request))
                .expectNextCount(1)
                .verifyComplete();

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

        Mockito.when(budgetProvider.first()).thenReturn(Mono.just(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
                Mono.just(budget));

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        StepVerifier.create(subject.createExpense(request))
                .assertNext(response -> {
                    assertThat(response.getExpenses()).hasSize(2);
                })
                .verifyComplete();

        verify(budget).createExpense("My new expense", 20, 40);
    }

    @Test
    void createExpense_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Mono.error(StatusException.notFound("No such element")));

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        StepVerifier.create(subject.createExpense(request))
                .expectError(StatusException.class)
                .verify();
    }

    @Test
    void computeExpense() {
        Mockito.when(budgetProvider.first()).thenReturn(Mono.just(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
                Mono.just(Budget.builder()
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

        StepVerifier.create(subject.computeExpense(1, 2019, 1))
                .expectNext(new ComputedExpenseResponse(35, 200, DateUtils.forMonth(2019, 1)))
                .verifyComplete();
    }
}
