package com.jongsoft.finance.rest.category.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.API;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;

class CategoryYearGraphResourceTest extends TestSetup {

    private CategoryYearGraphResource subject;

    private FilterFactory filterFactory;
    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private CategoryProvider categoryProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject= new CategoryYearGraphResource(
                new ResourceBundleMessageSource("i18n.messages"),
                filterFactory,
                transactionProvider,
                categoryProvider);
    }

    @Test
    void chart() {
        final Category category = Category.builder()
                .id(1L)
                .label("grocery")
                .description("For groceries")
                .lastActivity(LocalDate.of(2019, 1, 2))
                .build();

        Mockito.when(categoryProvider.lookup()).thenReturn(API.List(category));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());

        subject.chart(2019, Locale.GERMAN, Currency.builder().symbol('E').build());

        var mockFilter = filterFactory.transaction();
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).categories(API.List(new EntityRef(category.getId())));
        Mockito.verify(mockFilter, Mockito.times(12)).onlyIncome(true);
        Mockito.verify(mockFilter, Mockito.times(12)).onlyIncome(false);
        DateRange.forYear(2019).months().forEach(range -> {
            Mockito.verify(mockFilter).range(range);
        });
        Mockito.verify(transactionProvider, Mockito.times(24)).balance(Mockito.any());
    }

}