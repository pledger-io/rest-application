package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.rule.RuleChangeJpa;
import com.jongsoft.finance.jpa.rule.RuleConditionJpa;
import com.jongsoft.finance.jpa.rule.RuleJpa;
import com.jongsoft.finance.messaging.commands.rule.ChangeConditionCommand;
import com.jongsoft.finance.messaging.commands.rule.ChangeRuleCommand;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;

class TransactionRuleListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql",
                "sql/transaction/rule-provider.sql"
        );
    }

    @Test
    void handleConditionChange() {
        setup();
        eventPublisher.publishEvent(
                new ChangeConditionCommand(1L, RuleColumn.BUDGET, RuleOperation.EQUALS, "rude"));

        var check = entityManager.find(RuleConditionJpa.class, 1L);
        Assertions.assertThat(check.getCondition()).isEqualTo("rude");
        Assertions.assertThat(check.getField()).isEqualTo(RuleColumn.BUDGET);
    }

    @Test
    void handleChange() {
        setup();
        eventPublisher.publishEvent(
                new ChangeRuleCommand(1L, RuleColumn.CATEGORY, "1"));

        var check = entityManager.find(RuleChangeJpa.class, 1L);
        Assertions.assertThat(check.getValue()).isEqualTo("1");
        Assertions.assertThat(check.getField()).isEqualTo(RuleColumn.CATEGORY);
    }

    @Test
    void handleSortedEvent() {
        setup();
        eventPublisher.publishEvent(
                new ReorderRuleCommand(2L, 2));

        var check = entityManager.find(RuleJpa.class, 2L);
        Assertions.assertThat(check.getSort()).isEqualTo(2);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
