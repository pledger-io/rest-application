package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.RegisterAccountIconCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class RegisterAccountIconHandler implements CommandHandler<RegisterAccountIconCommand> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;

    @Inject
    RegisterAccountIconHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RegisterAccountIconCommand command) {
        log.info("[{}] - Processing icon registration event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("imageFileToken", command.fileCode())
                .fieldEq("id", command.id())
                .execute();
    }
}
