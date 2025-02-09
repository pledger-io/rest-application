package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.AdjustSavingGoalCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class AdjustSavingGoalHandler implements CommandHandler<AdjustSavingGoalCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public AdjustSavingGoalHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(AdjustSavingGoalCommand command) {
        log.info("[{}] - Adjusting a saving goal.", command.id());

        entityManager.update(SavingGoalJpa.class)
                .set("targetDate", command.targetDate())
                .set("goal", command.goal())
                .fieldEq("id", command.id())
                .execute();
    }
}
