package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.http.HttpHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

class ProfileExportResourceTest {

    private ProfileExportResource subject;

    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private StorageService storageService;
    @Mock
    private AccountProvider accountProvider;
    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private BudgetProvider budgetProvider;
    @Mock
    private TagProvider tagProvider;
    @Mock
    private ExpenseProvider expenseProvider;
    @Mock
    private TransactionRuleProvider transactionRuleProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(accountProvider.supports(Mockito.any())).thenCallRealMethod();
        Mockito.when(accountProvider.lookup()).thenReturn(Collections.List());
        Mockito.when(categoryProvider.supports(Mockito.any())).thenCallRealMethod();
        Mockito.when(categoryProvider.lookup()).thenReturn(Collections.List());
        Mockito.when(budgetProvider.supports(Mockito.any())).thenCallRealMethod();
        Mockito.when(budgetProvider.lookup()).thenReturn(Collections.List());
        Mockito.when(tagProvider.supports(Mockito.any())).thenCallRealMethod();
        Mockito.when(tagProvider.lookup()).thenReturn(Collections.List());
        Mockito.when(transactionRuleProvider.supports(Mockito.any())).thenCallRealMethod();
        Mockito.when(transactionRuleProvider.lookup()).thenReturn(Collections.List());

        subject = new ProfileExportResource(
                authenticationFacade,
                List.of(
                        accountProvider,
                        categoryProvider,
                        budgetProvider,
                        tagProvider,
                        transactionRuleProvider),
                List.of(
                        accountProvider,
                        categoryProvider,
                        expenseProvider),
                storageService);
    }

    @Test
    void export() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("sample@gmail.com");

        var response = subject.export().blockingGet();

        Assertions.assertThat(response.status().getCode()).isEqualTo(200);
        Assertions.assertThat(response.header(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");
        Assertions.assertThat(response.header(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=\"sample@gmail.com-profile.json\"");
    }
}
