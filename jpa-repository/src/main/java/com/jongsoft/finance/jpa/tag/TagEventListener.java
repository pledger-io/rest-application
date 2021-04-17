package com.jongsoft.finance.jpa.tag;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.events.TagCreatedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

@Singleton
@Transactional
public class TagEventListener extends RepositoryJpa {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public TagEventListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleTagCreated(TagCreatedEvent event) {
        logger.trace("[{}] - Processing tag creation event", event.getTag());

        var toCreate = TagJpa.builder()
                .name(event.getTag())
                .user(activeUser())
                .archived(false)
                .build();

        entityManager.persist(toCreate);
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select u from UserAccountJpa u where u.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }
}
