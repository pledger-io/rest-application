package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Singleton
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

        var hql = """
                update ImportJpa
                set finished = :finished
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("finished", new Date())
                .execute();
    }

}
