package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
public class TransactionScheduleHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(TransactionScheduleHandler.class);
    private final ReactiveEntityManager entityManager;

    public TransactionScheduleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    void handleCreate(CreateScheduleCommand command) {
        log.info("[{}] - Processing schedule create event", command.name());
        var from = entityManager.getById(AccountJpa.class, command.from());
        var to = entityManager.getById(AccountJpa.class, command.destination());

        var transactionSchedule = new TransactionScheduleJpa(
                command.amount(),
                command.name(),
                command.schedule().periodicity(),
                command.schedule().interval(),
                entityManager.currentUser(),
                from,
                to);

        entityManager.persist(transactionSchedule);
    }

    @EventListener
    void handleDescribe(DescribeScheduleCommand command) {
        log.info("[{}] - Processing schedule describe event", command.id());

        entityManager
                .update(TransactionScheduleJpa.class)
                .set("description", command.description())
                .set("name", command.name())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleLimit(LimitScheduleCommand command) {
        log.info("[{}] - Processing schedule limit event", command.id());

        entityManager
                .update(TransactionScheduleJpa.class)
                .set("start", command.start())
                .set("end", command.end())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleScheduleRan(TransactionScheduleRan command) {
        log.info("[{}] - Processing schedule ran event", command.id());

        entityManager
                .update(TransactionScheduleJpa.class)
                .set("lastRun", command.runDate())
                .set("nextRun", command.nextRunDate())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    void handleReschedule(RescheduleCommand command) {
        log.info("[{}] - Processing schedule reschedule event", command.id());

        entityManager
                .update(TransactionScheduleJpa.class)
                .set("interval", command.schedule().interval())
                .set("periodicity", command.schedule().periodicity())
                .fieldEq("id", command.id())
                .execute();
    }
}
