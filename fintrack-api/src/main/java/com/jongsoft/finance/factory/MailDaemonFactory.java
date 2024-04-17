package com.jongsoft.finance.factory;

import com.jongsoft.finance.core.MailDaemon;
import io.micronaut.context.annotation.*;
import io.micronaut.email.BodyType;
import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import io.micronaut.email.MultipartBody;
import io.micronaut.email.template.TemplateBody;
import io.micronaut.views.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class MailDaemonFactory {
    private final Logger log = LoggerFactory.getLogger(MailDaemonFactory.class);

    @Context
    @Requirements({
            @Requires(property = "application.mail", notEquals = "mock"),
    })
    @Replaces(MailDaemon.class)
    public MailDaemon createMailDaemon(
            @Value("${application.mail}") String mailImplementation,
            EmailSender<?, ?> customMailer) {
        log.info("Starting a real mail daemon using {}", mailImplementation);
        return (recipient, template, mailProperties) -> {
            log.debug("Sending email to {}", recipient);

            var email = Email.builder()
                    .to(recipient)
                    .subject("Pleger.io: Welcome to the family!")
                    .body(new MultipartBody(
                            new TemplateBody<>(BodyType.HTML, new ModelAndView<>(template + ".html", mailProperties)),
                            new TemplateBody<>(BodyType.TEXT, new ModelAndView<>(template + ".text", mailProperties)))
                    );

            customMailer.send(email);
        };
    }

    @Context
    public MailDaemon createMailDaemon() {
        log.info("Starting a mock mail daemon");
        return (recipient, template, mailProperties) -> log.info("Sending email to {}", recipient);
    }
}
