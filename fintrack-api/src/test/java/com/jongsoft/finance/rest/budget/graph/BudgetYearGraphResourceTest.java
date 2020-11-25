package com.jongsoft.finance.rest.budget.graph;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;

class BudgetYearGraphResourceTest extends TestSetup {

    private FilterFactory filterFactory;
    @Mock
    private BudgetProvider budgetProvider;
    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private BudgetYearGraphResource subject;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject = new BudgetYearGraphResource(
                filterFactory,
                budgetProvider,
                transactionProvider,
                currentUserProvider,
                new ResourceBundleMessageSource("i18n.messages"));

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        Mockito.when(budgetProvider.lookup(Mockito.eq(2019), Mockito.anyInt())).thenReturn(
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
    }

    @Test
    void expense() {
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());

        subject.expense(Locale.GERMAN, 2019);

        Mockito.verify(budgetProvider, Mockito.times(12)).lookup(Mockito.eq(2019), Mockito.anyInt());
        Mockito.verify(transactionProvider, Mockito.times(12)).balance(Mockito.any());
    }

    @Test
    void income() {
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());

        subject.income(Locale.GERMAN, 2019);

        Mockito.verify(budgetProvider, Mockito.times(12)).lookup(Mockito.eq(2019), Mockito.anyInt());
        Mockito.verify(transactionProvider, Mockito.times(12)).balance(Mockito.any());
    }
}
