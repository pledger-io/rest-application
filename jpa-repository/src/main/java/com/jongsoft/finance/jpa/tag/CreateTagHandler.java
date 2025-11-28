package com.jongsoft.finance.jpa.tag;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.tag.CreateTagCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateTagHandler implements CommandHandler<CreateTagCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateTagHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateTagCommand command) {
        log.info("[{}] - Processing tag creation event", command.tag());

        var toCreate = TagJpa.builder()
                .name(command.tag())
                .user(entityManager.currentUser())
                .archived(false)
                .build();

        entityManager.persist(toCreate);
    }
}
