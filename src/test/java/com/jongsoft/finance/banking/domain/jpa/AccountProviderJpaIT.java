package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.AccountTypeProvider;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Database - Accounts")
class AccountProviderJpaIT extends JpaTestSetup {

    @Inject
    private AccountProvider accountProvider;

    @Inject
    private AccountTypeProvider accountTypeProvider;

    private Sequence<String> ownTypes;

    @Inject
    private FilterProvider<AccountProvider.FilterCommand> filterFactory;

    @BeforeEach
    void setup() {
        ownTypes = Collections.List(accountTypeProvider.lookup(false));
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/account/account-provider.sql");
    }

    @Test
    @DisplayName("Check account synonym")
    void ofSynonym_accountOne() {
        var account = accountProvider.synonymOf("Account trial").get();

        Assertions.assertThat(account.getName()).isEqualTo("Account One");
    }

    @Test
    @DisplayName("Check account synonym - not my account")
    void ofSynonym_notMyAccount() {
        Assertions.assertThat(accountProvider.synonymOf("Account Junk").isPresent())
                .isFalse();
    }

    @Test
    @DisplayName("Lookup all accounts")
    void lookup() {
        var all = accountProvider.lookup();
        Assertions.assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("Lookup own accounts")
    void lookup_ownAccounts() {
        var filter = filterFactory.create().types(ownTypes);

        var resultPage = accountProvider.lookup(filter);

        Assertions.assertThat(resultPage.total()).isEqualTo(1);
        Assertions.assertThat(resultPage.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    @DisplayName("Lookup account by name")
    void lookup_name() {
        var account = accountProvider.lookup("Account One").get();

        Assertions.assertThat(account.getIban()).isEqualTo("NLJND200001928233");
        Assertions.assertThat(account.getDescription()).isEqualTo("Demo Account");
        Assertions.assertThat(account.getName()).isEqualTo("Account One");
        Assertions.assertThat(account.getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(account.getNumber()).isBlank();
        Assertions.assertThat(account.getBic()).isBlank();
        Assertions.assertThat(account.getType()).isEqualTo("default");
        Assertions.assertThat(account.getUser()).isEqualTo(new UserIdentifier("demo-user"));
    }

    @Test
    @DisplayName("Lookup account paging 1")
    void lookup_page1() {
        var filter = filterFactory.create().page(0, 1);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content())
                .hasSize(1)
                .extracting("name")
                .contains("Account One");
    }

    @Test
    @DisplayName("Lookup account paging 2")
    void lookup_page2() {
        var filter = filterFactory.create().page(1, 1);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content()).isEmpty();
    }

    @Test
    @DisplayName("Lookup account partial name")
    void lookup_partialName() {
        var filter = filterFactory.create().name("one", false);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    @DisplayName("Lookup account by IBAN")
    void lookup_iban() {
        var filter = filterFactory.create().iban("NLJND200001928233", true);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    @DisplayName("Lookup account by IBAN - partial")
    void lookup_ibanPartial() {
        var filter = filterFactory.create().iban("NLJND20000", false).page(0, 50);

        var check = accountProvider.lookup(filter);

        Assertions.assertThat(check.total()).isEqualTo(1);
        Assertions.assertThat(check.content().head().getIban()).isEqualTo("NLJND200001928233");
    }

    @Test
    @DisplayName("Lookup system account")
    void lookup_systemType() {
        Assertions.assertThat(
                        accountProvider.lookup(SystemAccountTypes.RECONCILE).isPresent())
                .isFalse();
    }

    @Test
    @DisplayName("Top accounts")
    void top() {
        var filter = filterFactory.create().types(ownTypes).page(0, 2);

        var result = accountProvider.top(
                filter, Dates.range(LocalDate.of(2019, 1, 1), LocalDate.of(2020, 1, 1)), false);

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.head().total()).isEqualTo(20.2);
    }
}
