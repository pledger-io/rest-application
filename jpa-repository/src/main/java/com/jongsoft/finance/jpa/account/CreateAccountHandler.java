package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class CreateAccountHandler implements CommandHandler<CreateAccountCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public CreateAccountHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateAccountCommand event) {
        log.trace("[{}] - Processing account create event", event.name());

        var toCreate = AccountJpa.builder()
                .name(event.name())
                .currency(entityManager.get(
                        CurrencyJpa.class,
                        Collections.Map("code", event.currency())))
                .type(entityManager.get(
                        AccountTypeJpa.class,
                        Collections.Map("label", event.type())))
                .user(entityManager.get(
                        UserAccountJpa.class,
                        Collections.Map("username", authenticationFacade.authenticated())))
                .build();

        entityManager.persist(toCreate);
    }

}
