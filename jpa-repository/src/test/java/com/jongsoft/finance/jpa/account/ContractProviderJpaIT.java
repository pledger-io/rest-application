package com.jongsoft.finance.jpa.account;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

class ContractProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ContractProvider contractProvider;

    @BeforeEach
    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/contract-provider.sql"
        );
    }

    @Test
    void lookup() {
        var check = contractProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check.head().getId()).isEqualTo(1L);
    }

    @Test
    void lookup_name() {
        var check = contractProvider.lookup("Test contract")
                .get();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
        Assertions.assertThat(check.getName()).isEqualTo("Test contract");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    void lookup_nameIncorrectUser() {
        Assertions.assertThat(contractProvider.lookup("In between"))
                .isEmpty();
    }

    @Test
    void search() {
        Assertions.assertThat(contractProvider.search("con"))
                .hasSize(1)
                .first()
                .extracting(Contract::getId)
                .isEqualTo(1L);
    }

    @Test
    void search_incorrectUser() {
        Assertions.assertThat(contractProvider.search("betwe"))
                .isEmpty();
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
