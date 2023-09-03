package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.FilterFactoryJpa;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.time.LocalDate;

class AccountProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private AccountProvider accountProvider;

    @Inject
    private AccountTypeProvider accountTypeProvider;

    private Sequence<String> ownTypes;
    private FilterFactory filterFactory = new FilterFactoryJpa();

    @BeforeEach
    void setup() {
        ownTypes = Collections.List(accountTypeProvider.lookup(false));

        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql"
        );
    }

    @Test
    void ofSynonym_accountOne() {
        var account = accountProvider.synonymOf("Account trial").block();

        Assertions.assertThat(account.getName()).isEqualTo("Account One");
    }

    @Test
    void ofSynonym_notMyAccount() {
        StepVerifier.create(accountProvider.synonymOf("Account Junk"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void lookup() {
        var all = accountProvider.lookup();
        Assertions.assertThat(all).hasSize(1);
    }

    @Test
    void lookup_ownAccounts() {
        var filter = filterFactory.account()
                .types(ownTypes);

        var resultPage = accountProvider.lookup(filter);

        Assertions.assertThat(resultPage.total()).isEqualTo(1);
        Assertions.assertThat(resultPage.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_name() {
        var account = accountProvider.lookup("Account One").block();

        Assertions.assertThat(account.getIban()).isEqualTo("NLJND200001928233");
        Assertions.assertThat(account.getDescription()).isEqualTo("Demo Account");
        Assertions.assertThat(account.getName()).isEqualTo("Account One");
        Assertions.assertThat(account.getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(account.getNumber()).isBlank();
        Assertions.assertThat(account.getBic()).isBlank();
        Assertions.assertThat(account.getType()).isEqualTo("default");
        Assertions.assertThat(account.getUser().getUsername()).isEqualTo("demo-user");
    }

    @Test
    void lookup_partialName() {
        var filter = filterFactory.account()
                .name("one", false);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_iban() {
        var filter = filterFactory.account()
                .iban("NLJND200001928233", true);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_ibanPartial() {
        var filter = filterFactory.account()
                .iban("NLJND20000", false)
                .page(0);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_systemType() {
        StepVerifier.create(accountProvider.lookup(SystemAccountTypes.RECONCILE))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void top() {
        var filter = filterFactory.account()
                .types(ownTypes)
                .pageSize(2);

        var result = accountProvider.top(
                filter,
                Dates.range(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2020, 1, 1)),
                false);

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.head().total()).isEqualTo(20.2);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
