package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionCreatedEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionDescribeEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionLimitEvent;
import com.jongsoft.finance.domain.transaction.events.ScheduledTransactionRescheduleEvent;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.transaction.entity.ScheduledTransactionJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Singleton
@Transactional
public class TransactionScheduleEventListener {

    private final EntityManager entityManager;
    private final Logger logger;

    public TransactionScheduleEventListener(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleCreated(ScheduledTransactionCreatedEvent event) {
        logger.trace("[{}] - Processing schedule create event", event.getName());

        var from = entityManager.find(AccountJpa.class, event.getFrom().getId());
        var to = entityManager.find(AccountJpa.class, event.getDestination().getId());

        var jpaEntity = ScheduledTransactionJpa.builder()
                .user(from.getUser())
                .source(from)
                .destination(to)
                .periodicity(event.getSchedule().periodicity())
                .interval(event.getSchedule().interval())
                .amount(event.getAmount())
                .name(event.getName())
                .build();
        entityManager.persist(jpaEntity);
    }

    @BusinessEventListener
    public void handleDescribe(ScheduledTransactionDescribeEvent event) {
        logger.trace("[{}] - Processing schedule describe event", event.getId());

        var hql = """
                update ScheduledTransactionJpa 
                set description = :description,
                    name = :name
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.setParameter("description", event.getDescription());
        query.setParameter("name", event.getName());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleLimit(ScheduledTransactionLimitEvent event) {
        logger.trace("[{}] - Processing schedule limit event", event.getId());

        var hql = """
                update ScheduledTransactionJpa t
                set t.start = :startDate,
                    t.end = :endDate
                where t.id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.setParameter("startDate", event.getStart());
        query.setParameter("endDate", event.getEnd());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleReschedule(ScheduledTransactionRescheduleEvent event) {
        logger.trace("[{}] - Processing schedule reschedule event", event.getId());

        var hql = """
                update ScheduledTransactionJpa 
                set interval = :interval,
                    periodicity = :periodicity
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.setParameter("interval", event.getSchedule().interval());
        query.setParameter("periodicity", event.getSchedule().periodicity());
        query.executeUpdate();
    }
}
