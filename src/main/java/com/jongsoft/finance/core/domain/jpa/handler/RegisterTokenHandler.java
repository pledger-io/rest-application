package com.jongsoft.finance.core.domain.jpa.handler;

import com.jongsoft.finance.core.domain.commands.RegisterTokenCommand;
import com.jongsoft.finance.core.domain.jpa.entity.AccountTokenJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
class RegisterTokenHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(RegisterTokenHandler.class);

    private final ReactiveEntityManager entityManager;

    @Inject
    public RegisterTokenHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    @EventListener
    public void handle(RegisterTokenCommand command) {
        log.info("[{}] - Registering new security token.", command.username());

        var userAccountJpa = entityManager
                .from(UserAccountJpa.class)
                .fieldEq("username", command.username())
                .singleResult()
                .get();

        var refreshJpa = new AccountTokenJpa(
                null, userAccountJpa, command.refreshToken(), command.expireDate(), null);

        entityManager.persist(refreshJpa);
    }
}
