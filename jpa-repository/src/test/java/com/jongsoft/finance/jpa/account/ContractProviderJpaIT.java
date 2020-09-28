package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.time.LocalDate;

public class ContractProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ContractProvider contractProvider;

    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/contract-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = contractProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check.head().getId()).isEqualTo(1L);
    }

    @Test
    void lookup_name() {
        setup();

        var check = contractProvider.lookup("Test contract")
                .blockingGet();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
        Assertions.assertThat(check.getName()).isEqualTo("Test contract");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    void lookup_nameIncorrectUser() {
        setup();

        Assertions.assertThat(contractProvider.lookup("In between").isEmpty().blockingGet()).isTrue();
    }

    @Test
    void search() {
        setup();

        TestSubscriber<Contract> subscriber = new TestSubscriber<>();

        contractProvider.search("conT")
                .subscribe(subscriber);

        subscriber.assertValueCount(1);
        subscriber.assertResult(Contract.builder().id(1L).build());
    }

    @Test
    void search_incorrectUser() {
        setup();

        TestSubscriber<Contract> subscriber = new TestSubscriber<>();

        contractProvider.search("betwe")
                .subscribe(subscriber);

        subscriber.assertNoValues();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
