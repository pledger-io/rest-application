package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.SavingGoalJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.core.domain.jpa.query.expression.Expressions;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
class SavingGoalChangeHandler {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(SavingGoalChangeHandler.class);
    private final ReactiveEntityManager entityManager;

    SavingGoalChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleCreate(CreateSavingGoalCommand command) {
        log.info("[{}] - Creating new saving goal.", command.name());

        SavingGoalJpa entity = SavingGoalJpa.of(
                command.goal(),
                command.targetDate(),
                command.name(),
                entityManager.getById(AccountJpa.class, command.accountId()));

        entityManager.persist(entity);
    }

    @EventListener
    public void handleReservation(RegisterSavingInstallmentCommand command) {
        log.info("[{}] - Incrementing allocation for saving goal.", command.id());

        entityManager
                .update(SavingGoalJpa.class)
                .set(
                        "allocated",
                        Expressions.addition(
                                Expressions.field("allocated"),
                                Expressions.value(command.amount())))
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleAdjustment(AdjustSavingGoalCommand command) {
        log.info("[{}] - Adjusting a saving goal.", command.id());

        entityManager
                .update(SavingGoalJpa.class)
                .set("targetDate", command.targetDate())
                .set("goal", command.goal())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleReschedule(AdjustScheduleCommand command) {
        log.info("[{}] - Adjusting schedule for a saving goal.", command.id());

        entityManager
                .update(SavingGoalJpa.class)
                .set("targetDate", command.schedulable().getEnd())
                .set("periodicity", command.schedulable().getSchedule().periodicity())
                .set("interval", command.schedulable().getSchedule().interval())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleCompleted(CompleteSavingGoalCommand command) {
        log.info("[{}] - Marking saving goal for completed.", command.id());

        entityManager
                .update(SavingGoalJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
