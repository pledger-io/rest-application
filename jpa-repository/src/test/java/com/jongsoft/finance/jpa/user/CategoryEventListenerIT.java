package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.messaging.commands.category.CreateCategoryCommand;
import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import com.jongsoft.finance.messaging.commands.category.RenameCategoryCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

class CategoryEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/user/category-provider.sql"
        );
    }

    @Test
    void handleCreatedEvent() {
        eventPublisher.publishEvent(new CreateCategoryCommand("Created tag", "Description of"));

        var check = entityManager.createQuery("from CategoryJpa where label = :label", CategoryJpa.class)
                .setParameter("label", "Created tag")
                .getSingleResult();

        Assertions.assertThat(check.getLabel()).isEqualTo("Created tag");
        Assertions.assertThat(check.getDescription()).isEqualTo("Description of");
    }

    @Test
    void handleRenamedEvent() {
        eventPublisher.publishEvent(new RenameCategoryCommand(
                1L,
                "Updated grocery",
                "Updated description"));

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.getLabel()).isEqualTo("Updated grocery");
        Assertions.assertThat(check.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void handleRemovedEvent() {
        eventPublisher.publishEvent(new DeleteCategoryCommand(1L));

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
