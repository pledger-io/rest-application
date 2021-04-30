package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.commands.tag.CreateTagCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;

class TagEventListenerIT extends JpaTestSetup {

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
                "sql/transaction/tag-provider.sql"
        );
    }

    @Test
    void handleTagCreated() {
        setup();
        eventPublisher.publishEvent(new CreateTagCommand("created-tag"));

        var query = entityManager.createQuery("select t from TagJpa t where t.name = 'created-tag'");
        var check = (TagJpa) query.getSingleResult();
        Assertions.assertThat(check.getName()).isEqualTo("created-tag");
        Assertions.assertThat(check.getUser().getUsername()).isEqualTo("demo-user");
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
