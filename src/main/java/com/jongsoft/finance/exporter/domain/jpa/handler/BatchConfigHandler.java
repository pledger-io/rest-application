package com.jongsoft.finance.exporter.domain.jpa.handler;

import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.exporter.domain.commands.CreateConfigurationCommand;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class BatchConfigHandler {

    private final Logger log = LoggerFactory.getLogger(BatchConfigHandler.class);
    private final ReactiveEntityManager entityManager;

    BatchConfigHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    void handleCreated(CreateConfigurationCommand command) {
        log.info("[{}] - Processing CSV configuration create event", command.name());

        var entity = new ImportConfig(
                command.name(), command.fileCode(), command.type(), entityManager.currentUser());

        entityManager.persist(entity);
    }
}
