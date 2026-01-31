package com.jongsoft.finance.banking.domain.jpa.handler;

import com.jongsoft.finance.banking.domain.commands.CreateAccountCommand;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.core.domain.commands.CreateExternalUserCommand;
import com.jongsoft.finance.core.domain.commands.CreateUserCommand;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

@Singleton
class CreateUserHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(CreateUserHandler.class);

    private final ExecutorService executorService;

    CreateUserHandler(@Named("default") ExecutorService executorService) {
        this.executorService = executorService;
    }

    @EventListener
    public void userWasCreated(CreateUserCommand command) {
        log.info("[{}] - Creating reconcile account for user.", command.username());
        createReconcileAccount(command.username());
    }

    @EventListener
    public void externalUserWasCreated(CreateExternalUserCommand command) {
        log.info("[{}] - Creating reconcile account for external user.", command.username());
        createReconcileAccount(command.username());
    }

    private void createReconcileAccount(String username) {
        executorService.submit(() -> {
            try {
                // must sleep to ensure the user account was created first
                Thread.sleep(Duration.ofSeconds(1));
                InternalAuthenticationEvent.authenticate(username);
                CreateAccountCommand.accountCreated(
                        "Reconcile Account", "EUR", SystemAccountTypes.RECONCILE.label());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
