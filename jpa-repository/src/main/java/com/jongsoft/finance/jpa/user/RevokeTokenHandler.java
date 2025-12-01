package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.RevokeTokenCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Singleton
@Transactional
public class RevokeTokenHandler implements CommandHandler<RevokeTokenCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public RevokeTokenHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RevokeTokenCommand command) {
        log.info("[{}] - Revoking security token.", command.token());

        entityManager
                .update(AccountTokenJpa.class)
                .set("expires", LocalDateTime.now())
                .fieldEq("refreshToken", command.token())
                .fieldGtOrEq("expires", LocalDateTime.now())
                .execute();
    }
}
