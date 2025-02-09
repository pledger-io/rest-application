package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeInterestCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class ChangeAccountInterestHandler implements CommandHandler<ChangeInterestCommand> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;

    @Inject
    ChangeAccountInterestHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeInterestCommand command) {
        log.info("[{}] - Processing account interest event", command.id());

        entityManager.update(AccountJpa.class)
                .set("interest", command.interest())
                .set("interestPeriodicity", command.periodicity())
                .fieldEq("id", command.id())
                .execute();
    }

}
