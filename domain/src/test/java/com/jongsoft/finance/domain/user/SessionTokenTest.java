package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.user.RevokeTokenCommand;
import com.jongsoft.lang.Dates;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

class SessionTokenTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        new EventBus(eventPublisher);
    }

    @Test
    void revoke() {
        SessionToken.builder()
                .id(1L)
                .token("my-sample-token")
                .validity(Dates.range(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1)))
                .build()
                .revoke();

        var captor = ArgumentCaptor.forClass(RevokeTokenCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertEquals("my-sample-token", captor.getValue().token());
    }

    @Test
    void revoke_alreadyExpired() {
        var token = SessionToken.builder()
                .id(1L)
                .token("my-sample-token")
                .validity(Dates.range(
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now()))
                .build();

        Assertions.assertThrows(
                StatusException.class,
                token::revoke,
                "Cannot revoke a session token that is already revoked.");
    }
}
