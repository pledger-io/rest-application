package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.jpa.importer.entity.ImportConfig;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CreateImportJobCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateImportJobHandler implements CommandHandler<CreateImportJobCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateImportJobHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateImportJobCommand command) {
        log.info("[{}] - Processing import create event", command.slug());

        var configJpa = entityManager.<ImportConfig>blocking()
                .hql("from ImportConfig where id = :id")
                .set("id", command.configId())
                .maybe()
                .getOrThrow(() ->
                        StatusException.notFound(
                                "Could not find the configuration for id " + command.configId()));

        var importJpa = ImportJpa.builder()
                .slug(command.slug())
                .config(configJpa)
                .user(configJpa.getUser())
                .fileCode(command.fileCode())
                .build();

        entityManager.persist(importJpa);
    }

}
