package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.TerminateAccountCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class TerminateAccountHandler implements CommandHandler<TerminateAccountCommand> {

    private final ReactiveEntityManager entityManager;

    public TerminateAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(TerminateAccountCommand command) {
        log.trace("[{}] - Processing account terminate event", command.id());

        entityManager.update()
                .hql("update AccountJpa a set a.archived = true where a.id = :id")
                .set("id", command.id())
                .execute();
    }

}
