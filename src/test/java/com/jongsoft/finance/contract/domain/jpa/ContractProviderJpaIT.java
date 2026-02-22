package com.jongsoft.finance.contract.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Database - Contracts")
class ContractProviderJpaIT extends JpaTestSetup {

    @Inject
    private ContractProvider contractProvider;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/account/contract-provider.sql");
    }

    @Test
    @DisplayName("Lookup all contracts")
    void lookup() {
        var check = contractProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check.head().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Lookup contract by name")
    void lookup_name() {
        var check = contractProvider.lookup("Test contract").get();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
        Assertions.assertThat(check.getName()).isEqualTo("Test contract");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    @DisplayName("Lookup contract by name - incorrect user")
    void lookup_nameIncorrectUser() {
        Assertions.assertThat(contractProvider.lookup("In between")).isEmpty();
    }

    @Test
    @DisplayName("Search contracts")
    void search() {
        Assertions.assertThat(contractProvider.search("con"))
                .hasSize(1)
                .first()
                .extracting(Contract::getId)
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("Search contracts - incorrect user")
    void search_incorrectUser() {
        Assertions.assertThat(contractProvider.search("betwe")).isEmpty();
    }
}
