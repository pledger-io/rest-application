package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class CreateScheduleHandler implements CommandHandler<CreateScheduleCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public CreateScheduleHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

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
