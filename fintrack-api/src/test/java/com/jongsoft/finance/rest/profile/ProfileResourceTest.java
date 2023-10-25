package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.user.ChangeMultiFactorCommand;
import com.jongsoft.finance.messaging.commands.user.RegisterTokenCommand;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.PasswordEncoder;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class ProfileResourceTest extends TestSetup {

    private ProfileResource subject;
    private CurrentUserProvider currentUserProvider;
    private UserProvider userProvider;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        currentUserProvider = Mockito.mock(CurrentUserProvider.class);
        userProvider = Mockito.mock(UserProvider.class);
        subject = new ProfileResource(new FinTrack(new PasswordEncoder()), currentUserProvider, userProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        new EventBus(eventPublisher);
    }

    @Test
    public void get() {
        Assertions.assertThat(subject.get())
                .isNotNull()
                .hasFieldOrPropertyWithValue("currency", "EUR")
                .hasFieldOrPropertyWithValue("profilePicture", null)
                .hasFieldOrPropertyWithValue("theme", "dark")
                .hasFieldOrPropertyWithValue("mfa", false);
    }

    @Test
    public void patch() {
        var request = new PatchProfileRequest();
        request.setCurrency("USD");
        request.setTheme("light");
        request.setPassword("updated-password");

        Assertions.assertThat(subject.patch(request))
                .isNotNull()
                .hasFieldOrPropertyWithValue("currency", "USD")
                .hasFieldOrPropertyWithValue("profilePicture", null)
                .hasFieldOrPropertyWithValue("theme", "light")
                .hasFieldOrPropertyWithValue("mfa", false);
    }

    @Test
    public void sessions() {
        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername()))
                .thenReturn(Collections.List(
                        SessionToken.builder()
                                .id(1L)
                                .description("Sample session token")
                                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                                .build()));

        Assertions.assertThat(subject.sessions())
                .isNotNull()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("description", "Sample session token");
    }

    @Test
    public void createSession() {
        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername()))
                .thenReturn(Collections.List(
                        SessionToken.builder()
                                .id(1L)
                                .description("Sample session token")
                                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                                .build()));

        subject.createSession(new TokenCreateRequest("sample description", LocalDate.now().plusDays(1)));

        verify(eventPublisher).publishEvent(Mockito.any(RegisterTokenCommand.class));
    }

    @Test
    public void revokeSession() {
        var token = Mockito.spy(SessionToken.builder()
                .id(1L)
                .description("Sample session token")
                .validity(Dates.range(LocalDateTime.now(), ChronoUnit.DAYS))
                .build());

        Mockito.when(userProvider.tokens(ACTIVE_USER.getUsername())).thenReturn(Collections.List(token));

        subject.deleteSession(1L);

        verify(token).revoke();
    }

    @Test
    public void enableMfa() {
        final Totp totp = new Totp(ACTIVE_USER.getSecret());
        var request = new MultiFactorRequest();
        request.setVerificationCode(totp.now());

        subject.enableMfa(request);

        var captor = ArgumentCaptor.forClass(ChangeMultiFactorCommand.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().username()).isEqualTo(ACTIVE_USER.getUsername());
        assertThat(captor.getValue().enabled()).isEqualTo(true);
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
