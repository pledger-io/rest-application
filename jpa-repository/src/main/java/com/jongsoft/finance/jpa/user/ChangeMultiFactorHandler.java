package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangeMultiFactorCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class ChangeMultiFactorHandler implements CommandHandler<ChangeMultiFactorCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public ChangeMultiFactorHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeMultiFactorCommand command) {
        log.info("[{}] - Updating multi factor setting", command.username());

        entityManager
                .update(UserAccountJpa.class)
                .set("twoFactorEnabled", command.enabled())
                .fieldEq("username", command.username().email())
                .execute();
    }
}
