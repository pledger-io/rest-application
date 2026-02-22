package com.jongsoft.finance.banking.domain.jpa.handler;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.banking.domain.commands.CreateTagCommand;
import com.jongsoft.finance.banking.domain.commands.DeleteTagCommand;
import com.jongsoft.finance.banking.domain.jpa.entity.TagJpa;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
class TagChangeHandler {

    private final Logger log = getLogger(TagChangeHandler.class);
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    TagChangeHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @EventListener
    public void handleCreate(CreateTagCommand command) {
        log.info("[{}] - Processing tag creation event", command.tag());

        var toCreate = new TagJpa(command.tag(), false, entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleDelete(DeleteTagCommand command) {
        log.info("[{}] - Processing tag deletion event", command.tag());

        entityManager
                .update(TagJpa.class)
                .set("archived", true)
                .fieldEq("name", command.tag())
                .fieldEq("user.username", authenticationFacade.authenticated())
                .execute();
    }
}
