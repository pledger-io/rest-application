package com.jongsoft.finance.jpa.importer;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.importer.events.BatchImportConfigCreatedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.importer.entity.CSVImportConfig;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

@Singleton
@Transactional
public class CSVConfigEventListener extends RepositoryJpa {

    private final EntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;
    private final Logger logger;

    public CSVConfigEventListener(EntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreatedEvent(BatchImportConfigCreatedEvent event) {
        logger.trace("[{}] - Processing CSV configuration create event", event.getName());

        var entity = CSVImportConfig.builder()
                .fileCode(event.getFileCode())
                .name(event.getName())
                .user(activeUser())
                .build();

        entityManager.persist(entity);
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select u from UserAccountJpa u where u.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }

}
