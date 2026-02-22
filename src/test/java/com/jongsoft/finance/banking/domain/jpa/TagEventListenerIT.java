package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.domain.commands.CreateTagCommand;
import com.jongsoft.finance.banking.domain.commands.DeleteTagCommand;
import com.jongsoft.finance.banking.domain.jpa.entity.TagJpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("database")
@DisplayName("Database - Tag mutations")
class TagEventListenerIT extends JpaTestSetup {
    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/transaction/tag-provider.sql");
    }

    @Test
    @DisplayName("Create new tag")
    void handleTagCreated() {
        setup();
        CreateTagCommand.tagCreated("created-tag");

        var query =
                entityManager.createQuery("select t from TagJpa t where t.name = 'created-tag'");
        var check = (TagJpa) query.getSingleResult();
        Assertions.assertThat(check.getName()).isEqualTo("created-tag");
        Assertions.assertThat(check.getUser().getUsername()).isEqualTo("demo-user");
    }

    @Test
    @DisplayName("Delete existing tag")
    void handleTagDeleted() {
        setup();

        DeleteTagCommand.tagDeleted("Sample");

        var check = entityManager
                .createQuery("select t from TagJpa t where t.id = 1", TagJpa.class)
                .getSingleResult();

        Assertions.assertThat(check.isArchived()).isTrue();
    }
}
