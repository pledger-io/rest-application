package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.TerminateAccountCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TerminateAccountHandler implements CommandHandler<TerminateAccountCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(TerminateAccountCommand command) {
        log.info("[{}] - Processing account terminate event", command.id());

        entityManager.update()
                .hql("update AccountJpa a set a.archived = true where a.id = :id")
                .set("id", command.id())
                .execute();
    }

}
