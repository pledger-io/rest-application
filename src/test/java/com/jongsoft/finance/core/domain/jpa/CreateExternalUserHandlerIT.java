package com.jongsoft.finance.core.domain.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.core.domain.commands.CreateExternalUserCommand;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.lang.Collections;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - External User")
class CreateExternalUserHandlerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql");
    }

    @Test
    @DisplayName("Create new external user")
    void handle() {
        CreateExternalUserCommand.externalUserCreated(
                "demo@account", "token444", Collections.List("admin"));

        var entity = entityManager
                .createQuery(
                        "from UserAccountJpa where username = 'demo@account'", UserAccountJpa.class)
                .getSingleResult();

        assertThat(entity.getUsername()).isEqualTo("demo@account");
        assertThat(entity.getRoles()).hasSize(1).extracting("name").containsExactly("admin");
    }

    @Test
    @DisplayName("Create existing external user")
    void handle_existing_user() {
        CreateExternalUserCommand.externalUserCreated(
                "external-user@account", "token444", Collections.List("admin"));
    }
}
