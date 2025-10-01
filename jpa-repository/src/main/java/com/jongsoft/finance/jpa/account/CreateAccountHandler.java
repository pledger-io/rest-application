package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;

import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresJpa
@Transactional
public class CreateAccountHandler implements CommandHandler<CreateAccountCommand> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReactiveEntityManager entityManager;

    @Inject
    CreateAccountHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateAccountCommand event) {
        log.info("[{}] - Processing account create event", event.name());

        var toCreate =
                AccountJpa.builder()
                        .name(event.name())
                        .currency(
                                entityManager
                                        .from(CurrencyJpa.class)
                                        .fieldEq("code", event.currency())
                                        .singleResult()
                                        .get())
                        .type(
                                entityManager
                                        .from(AccountTypeJpa.class)
                                        .fieldEq("label", event.type())
                                        .singleResult()
                                        .get())
                        .user(entityManager.currentUser())
                        .build();

        entityManager.persist(toCreate);
    }
}
