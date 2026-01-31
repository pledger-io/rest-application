package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.RegisterSynonymCommand;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountSynonymJpa;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
class RegisterSynonymHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    RegisterSynonymHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @EventListener
    public void handle(RegisterSynonymCommand command) {
        log.info("[{}] - Processing register synonym event", command.accountId());

        var existingId = entityManager
                .from(AccountSynonymJpa.class)
                .fieldEq("synonym", command.synonym())
                .fieldEq("account.user.username", authenticationFacade.authenticated())
                .projectSingleValue(Long.class, "id");

        var account = entityManager
                .from(AccountJpa.class)
                .joinFetch("currency")
                .joinFetch("user")
                .fieldEq("id", command.accountId())
                .singleResult()
                .get();

        if (existingId.isPresent()) {
            entityManager
                    .update(AccountSynonymJpa.class)
                    .set("account", account)
                    .fieldEq("id", existingId.get())
                    .execute();
        } else {
            var entity = new AccountSynonymJpa(command.synonym(), account);
            entityManager.persist(entity);
        }
    }
}
