package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

        when(accountProvider.supports(Mockito.any())).thenCallRealMethod();
        when(accountProvider.lookup()).thenReturn(Collections.List());
        when(categoryProvider.supports(Mockito.any())).thenCallRealMethod();
        when(categoryProvider.lookup()).thenReturn(Collections.List());
        when(budgetProvider.supports(Mockito.any())).thenCallRealMethod();
        when(budgetProvider.lookup()).thenReturn(Collections.List());
        when(tagProvider.supports(Mockito.any())).thenCallRealMethod();
        when(tagProvider.lookup()).thenReturn(Collections.List());
        when(transactionRuleProvider.supports(Mockito.any())).thenCallRealMethod();
        when(transactionRuleProvider.lookup()).thenReturn(Collections.List());

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
        when(authenticationFacade.authenticated()).thenReturn("sample@gmail.com");

        Assertions.assertThat(subject.export())
                .isNotNull()
                .isInstanceOfSatisfying(HttpResponse.class, response -> {
                    assertThat(response.status().getCode()).isEqualTo(200);
                    assertThat(response.header(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");
                    assertThat(response.header(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=\"sample@gmail.com-profile.json\"");
                });
    }
}
