package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CloseBudgetHandler implements CommandHandler<CloseBudgetCommand> {

    private final ReactiveEntityManager entityManager;

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
