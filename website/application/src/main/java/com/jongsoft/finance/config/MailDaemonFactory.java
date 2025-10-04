package com.jongsoft.finance.config;

import com.jongsoft.finance.core.MailDaemon;

import io.micronaut.context.annotation.*;
import io.micronaut.email.*;
import io.micronaut.email.javamail.sender.JavaxEmailComposer;
import io.micronaut.email.javamail.sender.JavaxEmailSender;
import io.micronaut.email.template.TemplateBody;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.views.ModelAndView;

import jakarta.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

@Factory
public class MailDaemonFactory {

    private final Logger logger = LoggerFactory.getLogger(MailDaemonFactory.class);

    @Context
    @Primary
    @Requirements(@Requires(env = "smtp"))
    public TransactionalEmailSender<?, ?> createTransactionSender(
            @Named(TaskExecutors.IO) ExecutorService executorService,
            JavaxEmailComposer javaxEmailComposer) {
        return new JavaxEmailSender(executorService, javaxEmailComposer);
    }

    @Context
    public MailDaemon createMailDaemon() {
        logger.info("Starting a mock mail daemon");
        return (recipient, _, _) -> logger.info("Sending email to {}", recipient);
    }

    @Context
    @Requirements({
        @Requires(property = "application.mail", notEquals = "mock"),
    })
    @Replaces(MailDaemon.class)
    public MailDaemon createMailDaemon(
            @Value("${application.mail}") String mailImplementation,
            EmailSender<?, ?> customMailer) {
        logger.info("Starting a real mail daemon using {}", mailImplementation);
        return (recipient, template, mailProperties) -> {
            logger.debug("Sending email to {}", recipient);

            var email = Email.builder()
                    .to(recipient)
                    .subject("Pleger.io: Welcome to the family!")
                    .body(new MultipartBody(
                            new TemplateBody<>(
                                    BodyType.HTML,
                                    new ModelAndView<>(template + ".html", mailProperties)),
                            new TemplateBody<>(
                                    BodyType.TEXT,
                                    new ModelAndView<>(template + ".text", mailProperties))));

            customMailer.send(email);
        };
    }
}
