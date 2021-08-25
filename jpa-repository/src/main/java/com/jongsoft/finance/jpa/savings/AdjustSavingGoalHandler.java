package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.AdjustSavingGoalCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class AdjustSavingGoalHandler implements CommandHandler<AdjustSavingGoalCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(AdjustSavingGoalCommand command) {
        log.info("[{}] - Adjusting a saving goal.", command.id());

        var hql = """
                update SavingGoalJpa
                set
                  targetDate = :targetDate,
                  goal = :goal
                where
                  id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("targetDate", command.targetDate())
                .set("goal", command.goal())
                .execute();
    }
}
