package com.jongsoft.finance.jpa.user;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.events.CategoryCreatedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRemovedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRenamedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.user.entity.CategoryJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

@Singleton
@Transactional
public class CategoryEventListener extends RepositoryJpa {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public CategoryEventListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreatedEvent(CategoryCreatedEvent event) {
        logger.trace("[{}] - Processing create event for category", event.getLabel());

        var entity = CategoryJpa.builder()
                .label(event.getLabel())
                .description(event.getDescription())
                .user(activeUser())
                .build();

        entityManager.persist(entity);
    }

    @BusinessEventListener
    public void handleRenamedEvent(CategoryRenamedEvent event) {
        logger.trace("[{}] - Processing rename event for category", event.getCategoryId());

        var hql = """
                update CategoryJpa 
                set label = :label,
                    description = :description
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getCategoryId());
        query.setParameter("label", event.getLabel());
        query.setParameter("description", event.getDescription());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleRemovedEvent(CategoryRemovedEvent event) {
        logger.trace("[{}] - Processing remove event for category", event.getId());

        var hql = """
                update CategoryJpa 
                set archived = true
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.executeUpdate();
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select a from UserAccountJpa a where a.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }

}
