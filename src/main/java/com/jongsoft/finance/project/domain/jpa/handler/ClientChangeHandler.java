package com.jongsoft.finance.project.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.project.domain.commands.ArchiveClientCommand;
import com.jongsoft.finance.project.domain.commands.CreateClientCommand;
import com.jongsoft.finance.project.domain.commands.UpdateClientCommand;
import com.jongsoft.finance.project.domain.jpa.entity.ClientJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class ClientChangeHandler {
    private final Logger log = LoggerFactory.getLogger(ClientChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    ClientChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreateClient(CreateClientCommand command) {
        log.info("[{}] - Processing client create event", command.name());

        var toCreate = ClientJpa.of(
                command.name(),
                command.email(),
                command.phone(),
                command.address(),
                entityManager.currentUser());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleUpdateClient(UpdateClientCommand command) {
        log.info("[{}] - Processing client update event", command.id());

        entityManager
                .update(ClientJpa.class)
                .set("name", command.name())
                .set("email", command.email())
                .set("phone", command.phone())
                .set("address", command.address())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleArchiveClient(ArchiveClientCommand command) {
        log.info("[{}] - Processing client archive event", command.id());

        entityManager
                .update(ClientJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
