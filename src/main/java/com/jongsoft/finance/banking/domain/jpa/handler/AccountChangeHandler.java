package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountTypeJpa;
import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
@Transactional
class AccountChangeHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(AccountChangeHandler.class);

    private final ReactiveEntityManager entityManager;

    AccountChangeHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    public void handleAccountChange(ChangeAccountCommand command) {
        log.info("[{}] - Processing account change event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("iban", command.iban())
                .set("bic", command.bic())
                .set("number", command.number())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleInterestChange(ChangeInterestCommand command) {
        log.info("[{}] - Processing account interest event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("interest", command.interest())
                .set("interestPeriodicity", command.periodicity())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleIconChange(RegisterAccountIconCommand command) {
        log.info("[{}] - Processing icon registration event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("imageFileToken", command.fileCode())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleRename(RenameAccountCommand command) {
        log.info("[{}] - Processing account rename event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("name", command.name())
                .set("description", command.description())
                .set(
                        "type",
                        entityManager
                                .from(AccountTypeJpa.class)
                                .fieldEq("label", command.type())
                                .singleResult()
                                .get())
                .set(
                        "currency",
                        entityManager
                                .from(CurrencyJpa.class)
                                .fieldEq("code", command.currency())
                                .singleResult()
                                .get())
                .fieldEq("id", command.id())
                .execute();
    }

    @EventListener
    public void handleCreateAccount(CreateAccountCommand event) {
        log.info("[{}] - Processing account create event", event.name());

        var toCreate = AccountJpa.of(
                event.name(),
                entityManager
                        .from(AccountTypeJpa.class)
                        .fieldEq("label", event.type())
                        .singleResult()
                        .get(),
                entityManager.currentUser(),
                entityManager
                        .from(CurrencyJpa.class)
                        .fieldEq("code", event.currency())
                        .singleResult()
                        .get());

        entityManager.persist(toCreate);
    }

    @EventListener
    public void handleTerminate(TerminateAccountCommand command) {
        log.info("[{}] - Processing account terminate event", command.id());

        entityManager
                .update(AccountJpa.class)
                .set("archived", true)
                .fieldEq("id", command.id())
                .execute();
    }
}
