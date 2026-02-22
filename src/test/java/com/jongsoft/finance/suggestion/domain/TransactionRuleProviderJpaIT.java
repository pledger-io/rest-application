package com.jongsoft.finance.suggestion.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleGroupProvider;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Transaction Rules")
class TransactionRuleProviderJpaIT extends JpaTestSetup {

    @Inject private TransactionRuleProvider ruleProvider;
    @Inject private TransactionRuleGroupProvider ruleGroupProvider;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql",
                "sql/transaction/rule-provider.sql");
    }

    @Test
    @DisplayName("Lookup all rules")
    void lookup() {
        var check = ruleProvider.lookup();
        assertThat(check).hasSize(2);
    }

    @Test
    @DisplayName("Lookup rules by group")
    void lookup_group() {
        Assertions.assertThat(ruleProvider.lookup("Grocery stores"))
                .hasSize(1)
                .first()
                .satisfies(rule -> assertThat(rule.getName()).isEqualTo("Income rule"));
    }

    @Test
    @DisplayName("Lookup all rule groups")
    void lookup_groups() {
        Assertions.assertThat(ruleGroupProvider.lookup())
            .hasSize(2)
            .first()
            .satisfies(
                rule -> Assertions.assertThat(rule.getName()).isEqualTo("Grocery stores"));
    }

    @Test
    @DisplayName("Lookup rule group by name")
    void lookup_group_name() {
        var check = ruleGroupProvider.lookup("Grocery stores");
        Assertions.assertThat(check.isPresent()).isTrue();
    }
}
