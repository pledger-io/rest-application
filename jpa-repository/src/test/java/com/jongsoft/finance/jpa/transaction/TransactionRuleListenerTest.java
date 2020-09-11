package com.jongsoft.finance.jpa.transaction;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleSortedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.transaction.entity.RuleJpa;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;

class TransactionRuleListenerTest extends JpaTestSetup {

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
    void handleSortedEvent() {
        setup();
        eventPublisher.publishEvent(new TransactionRuleSortedEvent(
                this,
                2L,
                2));

        var check = entityManager.find(RuleJpa.class, 2L);
        Assertions.assertThat(check.getSort()).isEqualTo(2);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
