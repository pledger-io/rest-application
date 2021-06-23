package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.CreateSavingGoalCommand;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class CreateSavingGoalHandler implements CommandHandler<CreateSavingGoalCommand> {

    private final ReactiveEntityManager entityManager;

    public CreateSavingGoalHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateSavingGoalCommand command) {
        log.info("[{}] - Creating new saving goal.", command.name());

        var entity = SavingGoalJpa.builder()
                .goal(command.goal())
                .targetDate(command.targetDate())
                .name(command.name())
                .account(entityManager.get(AccountJpa.class, Collections.Map("id", command.accountId())))
                .build();

        entityManager.persist(entity);
    }

}
