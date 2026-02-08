package com.jongsoft.finance.suggestion.domain;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.suggestion.domain.commands.*;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleChangeJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleConditionJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleGroupJpa;
import com.jongsoft.finance.suggestion.domain.jpa.entity.RuleJpa;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.finance.suggestion.types.RuleOperation;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Transaction Rule mutations")
class TransactionRuleListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql",
                "sql/transaction/rule-provider.sql"
        );
    }

    @Test
    @DisplayName("Deal with condition for a rule")
    void handleConditionChange() {
        ChangeConditionCommand.changeConditionUpdated(1L, RuleColumn.BUDGET, RuleOperation.EQUALS, "rude");

        var check = entityManager.find(RuleConditionJpa.class, 1L);
        Assertions.assertThat(check.getCondition()).isEqualTo("rude");
        Assertions.assertThat(check.getField()).isEqualTo(RuleColumn.BUDGET);
    }

    @Test
    @DisplayName("Deal with change for a rule")
    void handleChange() {
        ChangeRuleCommand.changeRuleUpdated(1L, RuleColumn.CATEGORY, "1");

        var check = entityManager.find(RuleChangeJpa.class, 1L);
        Assertions.assertThat(check.getValue()).isEqualTo("1");
        Assertions.assertThat(check.getField()).isEqualTo(RuleColumn.CATEGORY);
    }

    @Test
    @DisplayName("Sort rule")
    void handleSortedEvent() {
        ReorderRuleCommand.reorderRuleUpdated(2L, 2);

        var check = entityManager.find(RuleJpa.class, 2L);
        Assertions.assertThat(check.getSort()).isEqualTo(2);
    }

    @Test
    @DisplayName("Remove rule")
    void removeRule() {
        RuleRemovedCommand.ruleRemoved(2L);

        var check = entityManager.find(RuleJpa.class, 2L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @Test
    @DisplayName("Create rule group")
    void handleCreateRuleGroup() {
        CreateRuleGroupCommand.ruleGroupCreated("group-name");

        var query = entityManager.createQuery("select t from RuleGroupJpa t where t.name = 'group-name'");
        var check = (RuleGroupJpa) query.getSingleResult();
        Assertions.assertThat(check.getName()).isEqualTo("group-name");
        Assertions.assertThat(check.getUser().getUsername()).isEqualTo("demo-user");
    }

    @Test
    @DisplayName("Rename rule group")
    void handleRenamedRuleGroup() {
        RenameRuleGroupCommand.ruleGroupRenamed(2L, "updated-name");

        var check = entityManager.find(RuleGroupJpa.class, 2L);
        Assertions.assertThat(check.getName()).isEqualTo("updated-name");
    }

    @Test
    @DisplayName("Sort rule group")
    void handleSortedRuleGroup() {
        ReorderRuleGroupCommand.reorderRuleGroupUpdated(2L, 0);

        var check = entityManager.find(RuleGroupJpa.class, 2L);
        Assertions.assertThat(check.getSort()).isEqualTo(0);
    }
}
