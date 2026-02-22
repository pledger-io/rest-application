package com.jongsoft.finance.exporter.domain.jpa.handler;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.exporter.domain.commands.CompleteImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.CreateImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.DeleteImportJobCommand;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportJpa;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Singleton
@Transactional
class BatchImportHandler {

    private final Logger log = LoggerFactory.getLogger(BatchImportHandler.class);
    private final ReactiveEntityManager entityManager;

    BatchImportHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    void handleCreate(CreateImportJobCommand command) {
        log.info("[{}] - Processing import create event", command.slug());

        var configJpa = entityManager
                .from(ImportConfig.class)
                .fieldEq("id", command.configId())
                .singleResult()
                .getOrThrow(() -> StatusException.notFound(
                        "Could not find the configuration for id " + command.configId()));

        var importJpa = new ImportJpa(
                command.slug(), command.fileCode(), configJpa, configJpa.getUser(), false);
        entityManager.persist(importJpa);
    }

    @EventListener
    void handleCompleted(CompleteImportJobCommand command) {
        log.info("[{}] - Processing import finished event", command.id());

        entityManager
                .update(ImportJpa.class)
                .set("finished", new Date())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleDelete(DeleteImportJobCommand command) {
        log.info("[{}] - Processing import deleted event", command.id());

        entityManager
                .update(ImportJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
