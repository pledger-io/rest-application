package com.jongsoft.finance.classification.domain.jpa.handler;

import com.jongsoft.finance.classification.domain.commands.CreateCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.DeleteCategoryCommand;
import com.jongsoft.finance.classification.domain.commands.RenameCategoryCommand;
import com.jongsoft.finance.classification.domain.jpa.entity.CategoryJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
class CategoryChangeHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(CategoryChangeHandler.class);
    private final ReactiveEntityManager entityManager;

    CategoryChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    void handleCreated(CreateCategoryCommand command) {
        log.info("[{}] - Processing create event for category", command.name());

        var entity =
                new CategoryJpa(command.name(), command.description(), entityManager.currentUser());

        entityManager.persist(entity);
    }

    @EventListener
    void handleDelete(DeleteCategoryCommand command) {
        log.info("[{}] - Processing remove event for category", command.id());

        entityManager
                .update(CategoryJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleRename(RenameCategoryCommand command) {
        log.info("[{}] - Processing rename event for category", command.id());

        entityManager
                .update(CategoryJpa.class)
                .set("label", command.name())
                .set("description", command.description())
                .fieldEq("id", command.id())
                .execute();
    }
}
