package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CompleteImporterJobHandler implements CommandHandler<CompleteImportJobCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CompleteImporterJobHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CompleteImportJobCommand command) {
        log.info("[{}] - Processing import finished event", command.id());

        entityManager
                .update(ImportJpa.class)
                .set("finished", new Date())
                .fieldEq("id", command.id())
                .execute();
    }
}
