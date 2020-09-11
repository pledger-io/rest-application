package com.jongsoft.finance.jpa.account;

import java.time.LocalDate;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.ContractProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

public class ContractProviderJpaTest extends JpaTestSetup {

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
        var check = contractProvider.lookup("Test contract");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getId()).isEqualTo(1L);
        Assertions.assertThat(check.get().getName()).isEqualTo("Test contract");
        Assertions.assertThat(check.get().getStartDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(check.get().getEndDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    void lookup_nameIncorrectUser() {
        setup();
        Assertions.assertThat(contractProvider.lookup("In between").isPresent()).isFalse();
    }

    @Test
    void search() {
        setup();
        var check = contractProvider.search("conT");

        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check.head().getId()).isEqualTo(1L);
    }

    @Test
    void search_incorrectUser() {
        setup();
        var check = contractProvider.search("betwe");

        Assertions.assertThat(check).hasSize(0);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
