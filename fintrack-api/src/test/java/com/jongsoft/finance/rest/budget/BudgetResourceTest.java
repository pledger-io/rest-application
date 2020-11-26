package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BudgetResourceTest extends TestSetup {

    private BudgetResource subject;

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private BudgetProvider budgetProvider;
    @Mock private ExpenseProvider expenseProvider;
    @Mock private TransactionProvider transactionProvider;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

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
                Maybe.just(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        var response = subject.firstBudget();
    }

    @Test
    void budget() {
        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(
                Single.just(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));

        var response = subject.budget(2019, 1).blockingGet();

        Assertions.assertThat(response.getIncome()).isEqualTo(200.20D);
        Assertions.assertThat(response.getPeriod().getFrom()).isEqualTo(LocalDate.of(2018, 2, 3));
        Assertions.assertThat(response.getExpenses()).hasSize(1);
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
        Mockito.verify(expenseProvider).lookup(Mockito.any(ExpenseProvider.FilterCommand.class));
        Mockito.verify(mockFilter).name("gro", false);
    }

    @Test
    void create() {
        Mockito.when(budgetProvider.first()).thenReturn(Maybe.empty());

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        var response = subject.create(request).blockingGet();

        Assertions.assertThat(response.getPeriod().getFrom()).isEqualTo(LocalDate.of(2019, 2, 1));
        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(BudgetCreatedEvent.class));
    }

    @Test
    void index_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Single.error(StatusException.notFound("No such element")));

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        assertThrows(StatusException.class, () -> subject.index(request).blockingGet());
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
                .thenReturn(Single.just(existingBudget));

        var request = BudgetCreateRequest.builder()
                .month(2)
                .year(2019)
                .income(2300.33D)
                .build();

        subject.index(request).blockingGet();

        Mockito.verify(existingBudget).indexBudget(LocalDate.of(2019, 2, 1), 2300.33D);
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

        Mockito.when(budgetProvider.first()).thenReturn(Maybe.just(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
                Single.just(budget));

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        var response = subject.createExpense(request).blockingGet();

        Mockito.verify(budget).createExpense("My new expense", 20, 40);
        Assertions.assertThat(response.getExpenses()).hasSize(2);
    }

    @Test
    void createExpense_noBudget() {
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Single.error(StatusException.notFound("No such element")));

        var request = ExpenseCreateRequest.builder()
                .name("My new expense")
                .lowerBound(20)
                .upperBound(40)
                .build();

        assertThrows(StatusException.class, () -> subject.createExpense(request).blockingGet());
    }

    @Test
    void computeExpense() {
        Mockito.when(budgetProvider.first()).thenReturn(Maybe.just(Budget.builder().start(LocalDate.MIN).build()));
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
                Single.just(Budget.builder()
                        .expectedIncome(200.20D)
                        .start(LocalDate.of(2018, 2, 3))
                        .expenses(Collections.List(Budget.Expense.builder()
                                .id(1L)
                                .name("Grocery")
                                .lowerBound(20D)
                                .upperBound(50D)
                                .build()))
                        .build()));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option(200D));

        TestSubscriber<ComputedExpenseResponse> testSubscriber = new TestSubscriber<>();

        subject.computeExpense(1, 2019, 1)
                .subscribe(testSubscriber);

        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertResult(new ComputedExpenseResponse(35, 200, DateUtils.forMonth(2019, 1)));
    }
}
