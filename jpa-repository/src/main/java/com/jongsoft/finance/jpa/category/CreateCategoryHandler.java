package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.category.CreateCategoryCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateCategoryHandler implements CommandHandler<CreateCategoryCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateCategoryHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateCategoryCommand command) {
        log.info("[{}] - Processing create event for category", command.name());

        var entity =
                CategoryJpa.builder()
                        .label(command.name())
                        .description(command.description())
                        .user(entityManager.currentUser())
                        .build();

        entityManager.persist(entity);
    }
}
