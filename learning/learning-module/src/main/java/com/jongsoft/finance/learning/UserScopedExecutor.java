package com.jongsoft.finance.learning;

import com.jongsoft.finance.learning.config.LearningExecutor;
import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.UserProvider;

import io.micronaut.context.event.ApplicationEventPublisher;

import jakarta.inject.Singleton;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Singleton
public final class UserScopedExecutor {

    private final UserProvider userProvider;
    private final ExecutorService executorService;
    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

    public UserScopedExecutor(
            UserProvider userProvider,
            @LearningExecutor ExecutorService executorService,
            ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher) {
        this.userProvider = userProvider;
        this.executorService = executorService;
        this.eventPublisher = eventPublisher;
    }

    public void runForPerUser(Runnable runnable) {
        for (var user : userProvider.lookup()) {
            executorService.submit(() -> runForUser(user.getUsername().email(), runnable));
        }
    }

    private void runForUser(String username, Runnable runnable) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        eventPublisher.publishEvent(new InternalAuthenticationEvent(this, username));
        runnable.run();
        MDC.remove("correlationId");
    }
}
