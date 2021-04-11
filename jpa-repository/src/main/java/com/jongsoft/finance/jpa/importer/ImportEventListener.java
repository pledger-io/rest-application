package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.importer.events.BatchImportCreatedEvent;
import com.jongsoft.finance.domain.importer.events.BatchImportDeletedEvent;
import com.jongsoft.finance.domain.importer.events.BatchImportFinishedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Date;

@Singleton
@Transactional
public class ImportEventListener extends RepositoryJpa {

    private final ReactiveEntityManager entityManager;
    private final Logger logger;

    public ImportEventListener(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreatedEvent(BatchImportCreatedEvent event) {
        logger.trace("[{}] - Processing import create event", event.getSlug());

        var configJpa = entityManager.<CSVImportConfig>blocking()
                .hql("from CSVImportConfig where id = :id")
                .set("id", event.getConfig().getId())
                .maybe()
                .getOrThrow(() ->
                        StatusException.notFound(
                                "Could not find the configuration for id " + event.getConfig().getId()));

        var importJpa = ImportJpa.builder()
                .slug(event.getSlug())
                .config(configJpa)
                .user(configJpa.getUser())
                .fileCode(event.getFileCode())
                .build();

        entityManager.persist(importJpa);
    }

    @BusinessEventListener
    public void handleFinishedEvent(BatchImportFinishedEvent event) {
        logger.trace("[{}] - Processing import finished event", event.getImportId());

        var hql = """
                update ImportJpa 
                set finished = :finished 
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", event.getImportId())
                .set("finished", new Date())
                .execute();
    }

    @BusinessEventListener
    public void handleDeletedEvent(BatchImportDeletedEvent event) {
        logger.trace("[{}] - Processing import deleted event", event.getId());

        var hql = """
                update ImportJpa
                set archived = true
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", event.getId())
                .execute();
    }

}
