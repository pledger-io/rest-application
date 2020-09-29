package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.jpa.FilterFactoryJpa;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

class AccountProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private AccountProvider accountProvider;

    @Inject
    private AccountTypeProvider accountTypeProvider;

    private Sequence<String> ownTypes;
    private FilterFactory filterFactory = new FilterFactoryJpa();

    void setup() {
        ownTypes = API.List(accountTypeProvider.lookup(false));

        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");

        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql"
        );
    }

    @Test
    void ofSynonym_accountOne() {
        setup();
        var account = accountProvider.synonymOf("Account trial").blockingGet();

        Assertions.assertThat(account.getName()).isEqualTo("Account One");
    }

    @Test
    void ofSynonym_notMyAccount() {
        setup();
        var account = accountProvider.synonymOf("Account Junk")
                .test();

        account.assertNoValues();
    }

    @Test
    void lookup() {
        setup();
        var all = accountProvider.lookup();
        Assertions.assertThat(all).hasSize(1);
    }

    @Test
    void lookup_ownAccounts() {
        setup();
        var filter = filterFactory.account()
                .types(ownTypes);

        var resultPage = accountProvider.lookup(filter);

        Assertions.assertThat(resultPage.total()).isEqualTo(1);
        Assertions.assertThat(resultPage.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_name() {
        setup();
        var account = accountProvider.lookup("Account One").blockingGet();

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
        setup();
        var filter = filterFactory.account()
                .name("one", false);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_iban() {
        setup();
        var filter = filterFactory.account()
                .iban("NLJND200001928233", true);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_ibanPartial() {
        setup();
        var filter = filterFactory.account()
                .iban("NLJND20000", false)
                .page(0);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    void lookup_systemType() {
        setup();
        var account = accountProvider.lookup(SystemAccountTypes.RECONCILE).test();

        account.assertNoValues();
        account.assertComplete();
    }

    @Test
    void top() {
        setup();
        var filter = filterFactory.account()
                .types(ownTypes)
                .pageSize(2);

        var result = accountProvider.top(filter, DateRange.forMonth(2019, 1));

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.head().total()).isEqualTo(20.2);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
