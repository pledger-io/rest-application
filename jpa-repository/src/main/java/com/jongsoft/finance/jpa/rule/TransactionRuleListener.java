package com.jongsoft.finance.jpa.rule;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleSortedEvent;

@Singleton
@Transactional
public class TransactionRuleListener {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public TransactionRuleListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleSortedEvent(TransactionRuleSortedEvent event) {
        logger.trace("[{}] - Processing transaction rule sort event", event.getRuleId());

        var jpaEntity = entityManager.find(RuleJpa.class, event.getRuleId());
        entityManager.detach(jpaEntity);

        var hql = """
                update RuleJpa 
                set sort = sort + :direction
                where sort between :fromSort and :untilSort
                 and id in (select id from RuleJpa rj 
                        where rj.user.username = :username 
                            and rj.group.name = :name)""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("name", jpaEntity.getGroup().getName());
        if ((event.getSort() - jpaEntity.getSort()) < 0) {
            query.setParameter("direction",1);
            query.setParameter("fromSort", event.getSort());
            query.setParameter("untilSort", jpaEntity.getSort());
        } else {
            query.setParameter("direction",-1);
            query.setParameter("fromSort", jpaEntity.getSort());
            query.setParameter("untilSort", event.getSort());
        }
        query.executeUpdate();

        entityManager.createQuery("update RuleJpa set sort = " + event.getSort() + " where id = " + event.getRuleId())
                .executeUpdate();
    }
}
