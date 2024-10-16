package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
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
    private final AuthenticationFacade authenticationFacade;

    @Inject
    CreateAccountHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateAccountCommand event) {
        log.info("[{}] - Processing account create event", event.name());

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
