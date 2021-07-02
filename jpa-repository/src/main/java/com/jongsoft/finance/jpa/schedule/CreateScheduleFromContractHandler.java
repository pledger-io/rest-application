package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.CreateScheduleForContractCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateScheduleFromContractHandler implements CommandHandler<CreateScheduleForContractCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(CreateScheduleForContractCommand command) {
        log.info("[{}] - Creating a schedule from an existing contract", command.name());

        var jpaEntity = ScheduledTransactionJpa.builder()
                .name(command.name())
                .amount(command.amount())
                .contract(entityManager.get(ContractJpa.class, Collections.Map("id", command.contract().getId())))
                .source(entityManager.get(AccountJpa.class, Collections.Map("id", command.source().getId())))
                .destination(entityManager.get(AccountJpa.class, Collections.Map("id", command.contract().getCompany().getId())))
                .periodicity(command.schedule().periodicity())
                .interval(command.schedule().interval())
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
                .build();

        entityManager.persist(jpaEntity);
    }

}
