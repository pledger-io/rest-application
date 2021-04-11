package com.jongsoft.finance.jpa.account.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeInterestCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangeAccountInterestHandler implements CommandHandler<ChangeInterestCommand> {

    private final ReactiveEntityManager entityManager;

    public ChangeAccountInterestHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeInterestCommand command) {
        log.trace("[{}] - Processing account interest event", command.id());

        var hql = """
                update AccountJpa 
                set interest = :interest,
                    interestPeriodicity = :periodicity
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("interest", command.interest())
                .set("periodicity", command.periodicity())
                .update();
    }

}
