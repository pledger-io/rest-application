package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.CompleteSavingGoalCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CompleteSavingGoalHandler implements CommandHandler<CompleteSavingGoalCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CompleteSavingGoalHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CompleteSavingGoalCommand command) {
        log.info("[{}] - Marking saving goal for completed.", command.id());

        entityManager.update(SavingGoalJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
