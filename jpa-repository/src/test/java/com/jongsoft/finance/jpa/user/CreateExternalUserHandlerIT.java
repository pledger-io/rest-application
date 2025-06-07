package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.commands.user.CreateExternalUserCommand;
import com.jongsoft.finance.messaging.commands.user.CreateUserCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CreateExternalUserHandlerIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql");
    }

    @Test
    void handle() {
        eventPublisher.publishEvent(new CreateExternalUserCommand(
                "demo@account",
                "token444",
                Collections.List("admin")));

        var entity = entityManager.createQuery("from UserAccountJpa where username = 'demo@account'", UserAccountJpa.class)
                .getSingleResult();

        assertThat(entity.getUsername()).isEqualTo("demo@account");
        assertThat(entity.getRoles())
                .hasSize(1)
                .extracting("name")
                .containsExactly("admin");
    }

    @Test
    void handle_existing_user() {
        eventPublisher.publishEvent(new CreateExternalUserCommand(
                "demo-user",
                "token444",
                Collections.List("admin")));
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
