package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.CreateAccountCommand;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.domain.commands.UserCreatedCommand;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
class CreateUserHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(CreateUserHandler.class);

    @EventListener
    public void userWasCreated(UserCreatedCommand command) {
        log.info("[{}] - Creating reconcile account for user.", command.username());
        CreateAccountCommand.accountCreated(
                "Reconcile Account", "EUR", SystemAccountTypes.RECONCILE.label());
    }
}
