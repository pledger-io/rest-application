package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.CompleteSavingGoalCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
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

        var hql = """
                update SavingGoalJpa
                set
                  archived = true
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .execute();
    }
}
