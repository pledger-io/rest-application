package com.jongsoft.finance.factory;

import io.micronaut.email.Contact;
import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Properties;

class MailDaemonFactoryTest {

    @Test
    void createMailDaemon() {
        // Given
        var subject = new MailDaemonFactory();
        var mockMailer = Mockito.mock(EmailSender.class);

        // When
        var mailDaemon = subject.createMailDaemon("smtp", mockMailer);
        mailDaemon.send("test@localhost", "test", new Properties());

        // Then
        Assertions.assertThat(mailDaemon)
                .isNotNull();

        var captor = ArgumentCaptor.<Email.Builder>captor();
        Mockito.verify(mockMailer, Mockito.times(1))
                .send(captor.capture());

        var email = captor.getValue().build();
        Assertions.assertThat(email)
                .isNotNull()
                .extracting(Email::getTo, Email::getSubject)
                .contains(List.of(new Contact("test@localhost")), "Pleger.io: Welcome to the family!");
    }

}