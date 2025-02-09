package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleForContractCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateScheduleFromContractHandler implements CommandHandler<CreateScheduleForContractCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateScheduleFromContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateScheduleForContractCommand command) {
        log.info("[{}] - Creating a schedule from an existing contract", command.name());

        var jpaEntity = ScheduledTransactionJpa.builder()
                .name(command.name())
                .amount(command.amount())
                .contract(entityManager.getById(ContractJpa.class, command.contract().getId()))
                .source(entityManager.getById(AccountJpa.class, command.source().getId()))
                .destination(entityManager.getById(AccountJpa.class, command.contract().getCompany().getId()))
                .periodicity(command.schedule().periodicity())
                .interval(command.schedule().interval())
                .user(entityManager.currentUser())
                .build();

        entityManager.persist(jpaEntity);
    }

}
