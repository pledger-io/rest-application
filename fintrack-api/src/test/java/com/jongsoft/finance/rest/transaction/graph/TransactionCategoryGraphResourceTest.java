package com.jongsoft.finance.rest.transaction.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

class TransactionCategoryGraphResourceTest extends TestSetup {

    private TransactionCategoryGraphResource subject;

    private FilterFactory filterFactory;
    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CurrencyProvider currencyProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterFactory = generateFilterMock();

        subject = new TransactionCategoryGraphResource(
                new ResourceBundleMessageSource("i18n.messages"),
                filterFactory,
                transactionProvider,
                categoryProvider,
                currentUserProvider,
                currencyProvider);
    }

    @Test
    void expenses() {
        final Category category = Category.builder()
                .id(1L)
                .description("Sample 1")
                .build();

        Mockito.when(categoryProvider.lookup()).thenReturn(API.List(category));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());

        subject.expenses(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 1, 31),
                Locale.GERMAN,
                Optional.of(Currency.builder().symbol('E').build()));

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider, Mockito.times(2)).balance(Mockito.any());
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
        Mockito.verify(mockFilter).categories(API.List(new EntityRef(1L)));
    }

    @Test
    void income() {
        final Category category = Category.builder()
                .id(1L)
                .description("Sample 1")
                .build();

        Mockito.when(categoryProvider.lookup()).thenReturn(API.List(category));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());

        subject.income(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 1, 31),
                Locale.GERMAN,
                Optional.of(Currency.builder().symbol('E').build()));

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider, Mockito.times(2)).balance(Mockito.any());
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).onlyIncome(true);
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
        Mockito.verify(mockFilter).categories(API.List(new EntityRef(1L)));
    }
}