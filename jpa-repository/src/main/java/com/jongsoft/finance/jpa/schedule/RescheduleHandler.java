package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.RescheduleCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class RescheduleHandler implements CommandHandler<RescheduleCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public RescheduleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RescheduleCommand command) {
        log.info("[{}] - Processing schedule reschedule event", command.id());

        entityManager.update(ScheduledTransactionJpa.class)
                .set("interval", command.schedule().interval())
                .set("periodicity", command.schedule().periodicity())
                .fieldEq("id", command.id())
                .execute();
    }

}
