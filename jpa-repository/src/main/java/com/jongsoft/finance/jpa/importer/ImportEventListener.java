package com.jongsoft.finance.jpa.importer;

import java.util.Date;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.importer.events.BatchImportCreatedEvent;
import com.jongsoft.finance.domain.importer.events.BatchImportFinishedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;

@Singleton
@Transactional
public class ImportEventListener extends RepositoryJpa {

    private final EntityManager entityManager;
    private final Logger logger;

    public ImportEventListener(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreatedEvent(BatchImportCreatedEvent event) {
        logger.trace("[{}] - Processing import create event", event.getSlug());

        var configJpa = entityManager.find(CSVImportConfig.class, event.getConfig().getId());
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

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getImportId());
        query.setParameter("finished", new Date());
        query.executeUpdate();
    }

}
