package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.messaging.commands.rule.CreateRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.RenameRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

class TransactionRuleGroupListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql"
        );
    }

    @Test
    void handleCreateEvent() {
        eventPublisher.publishEvent(new CreateRuleGroupCommand("group-name"));

        var query = entityManager.createQuery("select t from RuleGroupJpa t where t.name = 'group-name'");
        var check = (RuleGroupJpa) query.getSingleResult();
        Assertions.assertThat(check.getName()).isEqualTo("group-name");
        Assertions.assertThat(check.getUser().getUsername()).isEqualTo("demo-user");
    }

    @Test
    void handleRenamedEvent() {
        eventPublisher.publishEvent(new RenameRuleGroupCommand(2L, "updated-name"));

        var check = entityManager.find(RuleGroupJpa.class, 2L);
        Assertions.assertThat(check.getName()).isEqualTo("updated-name");
    }

    @Test
    void handleSortedEvent() {
        eventPublisher.publishEvent(new ReorderRuleGroupCommand(2L, 0));

        var check = entityManager.find(RuleGroupJpa.class, 2L);
        Assertions.assertThat(check.getSort()).isEqualTo(0);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
