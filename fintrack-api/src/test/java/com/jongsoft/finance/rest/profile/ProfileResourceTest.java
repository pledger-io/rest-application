package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.domain.user.events.UserAccountMultiFactorEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class ProfileResourceTest extends TestSetup {

    private ProfileResource subject;
    private CurrentUserProvider currentUserProvider;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        currentUserProvider = Mockito.mock(CurrentUserProvider.class);
        subject = new ProfileResource(currentUserProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        new EventBus(eventPublisher);
    }

    @Test
    public void get() {
        var result = subject.get().blockingGet();

        Assertions.assertThat(result.getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(result.getProfilePicture()).isNull();
        Assertions.assertThat(result.getTheme()).isEqualTo("dark");
        Assertions.assertThat(result.isMfa()).isEqualTo(false);
    }

    @Test
    public void patch() {
        var request = new PatchProfileRequest();
        request.setCurrency("USD");
        request.setTheme("light");
        request.setPassword("updated-password");

        var result = subject.patch(request).blockingGet();

        Assertions.assertThat(result.getTheme()).isEqualTo("light");
        Assertions.assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    public void sessions() {
        subject.sessions()
                .test()
                .assertComplete()
                .assertValue(token -> "Sample session token".equals(token.getDescription()))
                .assertValue(token -> LocalDateTime.now()
                        .plusDays(1)
                        .truncatedTo(ChronoUnit.MINUTES)
                        .equals(token.getValidUntil().truncatedTo(ChronoUnit.MINUTES)));
    }

    @Test
    public void enableMfa() {
        final Totp totp = new Totp(ACTIVE_USER.getSecret());
        var request = new MultiFactorRequest();
        request.setVerificationCode(totp.now());

        subject.enableMfa(request);

        var captor = ArgumentCaptor.forClass(UserAccountMultiFactorEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().getUsername()).isEqualTo(ACTIVE_USER.getUsername());
        Assertions.assertThat(captor.getValue().isEnabled()).isEqualTo(true);
    }

//    @Test
//    public void disableMfa() {
//        subject.disableMfa();
//
//        var captor = ArgumentCaptor.forClass(UserAccountMultiFactorEvent.class);
//        Mockito.verify(eventPublisher).publishEvent(captor.capture());
//
//        Assertions.assertThat(captor.getValue().getUsername()).isEqualTo(ACTIVE_USER.getUsername());
//        Assertions.assertThat(captor.getValue().isEnabled()).isEqualTo(false);
//    }
}
