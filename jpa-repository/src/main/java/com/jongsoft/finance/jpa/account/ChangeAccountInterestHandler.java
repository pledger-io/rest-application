package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeInterestCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class ChangeAccountInterestHandler implements CommandHandler<ChangeInterestCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    ChangeAccountInterestHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeInterestCommand command) {
        log.info("[{}] - Processing account interest event", command.id());

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
                .execute();
    }

}
