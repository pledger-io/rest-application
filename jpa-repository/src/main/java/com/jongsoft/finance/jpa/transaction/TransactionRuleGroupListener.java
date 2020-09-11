package com.jongsoft.finance.jpa.transaction;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupCreatedEvent;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupRenamedEvent;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupSortedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.transaction.entity.RuleGroupJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.lang.API;

@Singleton
@Transactional
public class TransactionRuleGroupListener extends RepositoryJpa {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public TransactionRuleGroupListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreateEvent(TransactionRuleGroupCreatedEvent event) {
        logger.trace("[{}] - Processing rule group create event", event.getName());

        var hql = """
                select max(sort) + 1 from RuleGroupJpa 
                where user.username = :username and archived = false""";
        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());

        var jpaEntity = RuleGroupJpa.builder()
                .name(event.getName())
                .user(activeUser())
                .sort(API.Option(this.<Integer>singleValue(query)).getOrSupply(() -> 1))
                .build();

        entityManager.persist(jpaEntity);
    }

    @BusinessEventListener
    public void handleRenamedEvent(TransactionRuleGroupRenamedEvent event) {
        logger.trace("[{}] - Processing rule group rename event", event.getRuleGroupId());

        var hql = """
                update RuleGroupJpa 
                set name = :name
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getRuleGroupId());
        query.setParameter("name", event.getName());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleSortedEvent(TransactionRuleGroupSortedEvent event) {
        logger.trace("[{}] - Processing rule group sorting event", event.getGroupId());

        var jpaEntity = entityManager.find(RuleGroupJpa.class, event.getGroupId());
        entityManager.detach(jpaEntity);

        var hql = """
                update RuleGroupJpa 
                set sort = sort + :direction
                where sort between :fromSort and :untilSort
                 and id in (select id from RuleGroupJpa rj where rj.user.username = :username)""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        if ((event.getSortOrder() - jpaEntity.getSort()) < 0) {
            query.setParameter("direction", 1);
            query.setParameter("fromSort", event.getSortOrder());
            query.setParameter("untilSort", jpaEntity.getSort());
        } else {
            query.setParameter("direction", -1);
            query.setParameter("fromSort", jpaEntity.getSort());
            query.setParameter("untilSort", event.getSortOrder());
        }
        query.executeUpdate();

        entityManager.createQuery("update RuleGroupJpa set sort = " + event.getSortOrder() + " where id = " + event.getGroupId())
                .executeUpdate();
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select u from UserAccountJpa u where u.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }
}
