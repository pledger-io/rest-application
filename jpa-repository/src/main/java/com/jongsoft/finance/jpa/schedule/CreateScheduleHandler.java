package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleCommand;
import com.jongsoft.lang.Collections;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateScheduleHandler implements CommandHandler<CreateScheduleCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(CreateScheduleCommand command) {
        log.info("[{}] - Processing schedule create event", command.name());
        var from = entityManager.get(AccountJpa.class, Collections.Map("id", command.from().getId()));
        var to = entityManager.get(AccountJpa.class, Collections.Map("id", command.destination().getId()));

        var jpaEntity = ScheduledTransactionJpa.builder()
                .user(from.getUser())
                .source(from)
                .destination(to)
                .periodicity(command.schedule().periodicity())
                .interval(command.schedule().interval())
                .amount(command.amount())
                .name(command.name())
                .build();

        entityManager.persist(jpaEntity);
    }

}
