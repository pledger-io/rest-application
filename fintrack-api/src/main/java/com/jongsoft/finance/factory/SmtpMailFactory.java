package com.jongsoft.finance.factory;

import io.micronaut.context.annotation.*;
import io.micronaut.email.TransactionalEmailSender;
import io.micronaut.email.javamail.sender.JavaxEmailComposer;
import io.micronaut.email.javamail.sender.JavaxEmailSender;
import io.micronaut.scheduling.TaskExecutors;

import jakarta.inject.Named;

import java.util.concurrent.ExecutorService;

@Factory
@Replaces(TransactionalEmailSender.class)
@Requirements(@Requires(env = "smtp"))
public class SmtpMailFactory {

    @Context
    @Primary
    public TransactionalEmailSender<?, ?> createTransactionSender(
            @Named(TaskExecutors.IO) ExecutorService executorService,
            JavaxEmailComposer javaxEmailComposer) {
        return new JavaxEmailSender(executorService, javaxEmailComposer);
    }
}
