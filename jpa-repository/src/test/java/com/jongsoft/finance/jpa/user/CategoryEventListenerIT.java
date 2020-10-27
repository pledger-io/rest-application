package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.events.CategoryCreatedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRemovedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRenamedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.CategoryJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;

class CategoryEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setUp() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/user/category-provider.sql"
        );
    }

    @Test
    void handleCreatedEvent() {
        setUp();
        eventPublisher.publishEvent(new CategoryCreatedEvent(
                this,
                "Created tag",
                "Description of"));

        var check = entityManager.createQuery("from CategoryJpa where label = :label", CategoryJpa.class)
                .setParameter("label", "Created tag")
                .getSingleResult();

        Assertions.assertThat(check.getLabel()).isEqualTo("Created tag");
        Assertions.assertThat(check.getDescription()).isEqualTo("Description of");
    }

    @Test
    void handleRenamedEvent() {
        setUp();
        eventPublisher.publishEvent(new CategoryRenamedEvent(
                this,
                1L,
                "Updated grocery",
                "Updated description"));

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.getLabel()).isEqualTo("Updated grocery");
        Assertions.assertThat(check.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void handleRemovedEvent() {
        setUp();
        eventPublisher.publishEvent(new CategoryRemovedEvent(
                this,
                1L));

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}