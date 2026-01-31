package com.jongsoft.finance.classification.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.classification.domain.commands.CreateCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.DeleteCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.RenameCategoryCommand;
import com.jongsoft.finance.classification.domain.jpa.entity.CategoryJpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Category mutations")
class CategoryEventListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/user/category-provider.sql");
    }

    @Test
    @DisplayName("Create new category")
    void handleCreatedEvent() {
        CreateCategoryCommand.categoryCreated("Created tag", "Description of");

        var check = entityManager
                .createQuery("from CategoryJpa where label = :label", CategoryJpa.class)
                .setParameter("label", "Created tag")
                .getSingleResult();

        Assertions.assertThat(check.getLabel()).isEqualTo("Created tag");
        Assertions.assertThat(check.getDescription()).isEqualTo("Description of");
    }

    @Test
    @DisplayName("Rename category")
    void handleRenamedEvent() {
        RenameCategoryCommand.categoryRenamed(1L, "Updated grocery", "Updated description");

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.getLabel()).isEqualTo("Updated grocery");
        Assertions.assertThat(check.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Remove category")
    void handleRemovedEvent() {
        DeleteCategoryCommand.categoryDeleted(1L);

        var check = entityManager.find(CategoryJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }
}
