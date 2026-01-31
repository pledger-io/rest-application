package com.jongsoft.finance.suggestion.domain;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleGroupProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Transaction Rule Groups")
class TransactionRuleGroupProviderJpaIT extends JpaTestSetup {

    @Inject private TransactionRuleGroupProvider ruleGroupProvider;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql");
    }

    @Test
    @DisplayName("Lookup all rule groups")
    void lookup() {
        Assertions.assertThat(ruleGroupProvider.lookup())
                .hasSize(2)
                .first()
                .satisfies(
                        rule -> Assertions.assertThat(rule.getName()).isEqualTo("Grocery stores"));
    }

    @Test
    @DisplayName("Lookup rule group by name")
    void lookup_name() {
        var check = ruleGroupProvider.lookup("Grocery stores");
        Assertions.assertThat(check.isPresent()).isTrue();
    }
}
