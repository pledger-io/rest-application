package com.jongsoft.finance.rest.category.graph;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;

class CategoryGraphResourceTest extends TestSetup {

    private CategoryGraphResource subject;

    private FilterFactory filterFactory;
    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private TransactionProvider transactionProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject = new CategoryGraphResource(
                new ResourceBundleMessageSource("i18n.messages"),
                filterFactory,
                categoryProvider,
                transactionProvider);
    }

    @Test
    void graph() {
        final Category category = Category.builder()
                .id(1L)
                .label("grocery")
                .description("For groceries")
                .lastActivity(LocalDate.of(2019, 1, 2))
                .build();

        Mockito.when(categoryProvider.lookup()).thenReturn(Collections.List(category));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());

        subject.graph(DateUtils.startOfMonth(2019, 1), DateUtils.startOfMonth(2019, 2), Locale.GERMAN);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
        Mockito.verify(mockFilter).categories(Collections.List(new EntityRef(category.getId())));
    }
}
