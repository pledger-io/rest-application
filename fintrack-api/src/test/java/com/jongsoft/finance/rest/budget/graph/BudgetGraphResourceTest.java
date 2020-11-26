package com.jongsoft.finance.rest.budget.graph;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.rest.TestSetup;
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

class BudgetGraphResourceTest extends TestSetup {

    private FilterFactory filterFactory;
    @Mock
    private BudgetProvider budgetProvider;
    @Mock
    private TransactionProvider transactionProvider;

    private BudgetGraphResource subject;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        var messageSource = new ResourceBundleMessageSource("i18n.messages");

        subject = new BudgetGraphResource(filterFactory, budgetProvider, transactionProvider, messageSource);
    }

    @Test
    void graph() {
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

        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());

        var response = subject.graph(
                Locale.GERMAN,
                DateUtils.startOfMonth(2019, 1),
                DateUtils.endOfMonth(2019, 1));

        Mockito.verify(budgetProvider).lookup(2019, 1);
        Mockito.verify(transactionProvider).balance(Mockito.any(TransactionProvider.FilterCommand.class));
    }

}
