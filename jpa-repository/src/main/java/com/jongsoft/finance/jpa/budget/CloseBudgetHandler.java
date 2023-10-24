package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CloseBudgetHandler implements CommandHandler<CloseBudgetCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    CloseBudgetHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CloseBudgetCommand command) {
        log.info("[{}] - Processing budget closing event", command.id());

        var hql = """
                update BudgetJpa
                set until = :end
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("end", command.end())
                .execute();
    }

}
