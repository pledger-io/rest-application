package com.jongsoft.finance.core.domain.jpa.handler;

import com.jongsoft.finance.core.domain.commands.RevokeTokenCommand;
import com.jongsoft.finance.core.domain.jpa.entity.AccountTokenJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.time.LocalDateTime;

@Singleton
class RevokeTokenHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(RevokeTokenHandler.class);

    private final ReactiveEntityManager entityManager;

    @Inject
    public RevokeTokenHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @EventListener
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
