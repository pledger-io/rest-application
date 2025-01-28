package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.commands.user.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

class UserEventListenerIT extends JpaTestSetup {

    private static final UserIdentifier DEMO_USER = new UserIdentifier("demo-user");


    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql");
    }

    @Test
    void handleUserAccountCreatedEvent() {
        eventPublisher.publishEvent(new CreateUserCommand("demo@account", "pasword123!"));

        var query = entityManager.createQuery("select a from UserAccountJpa a where a.username = 'demo@account'");
        var check = (UserAccountJpa) query.getSingleResult();

        Assertions.assertThat(check.getUsername()).isEqualTo("demo@account");
        Assertions.assertThat(check.getPassword()).isEqualTo("pasword123!");
        Assertions.assertThat(check.getTwoFactorSecret()).isNotEmpty();
        Assertions.assertThat(check.getRoles()).isNotEmpty();
        Assertions.assertThat(check.getRoles()).containsOnly(
                new RoleJpa(null, "admin", null),
                new RoleJpa(null, "accountant", null));
    }

    @Test
    void handleUserAccountPasswordEvent() {
        eventPublisher.publishEvent(new ChangePasswordCommand(DEMO_USER, "updated password"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getPassword()).isEqualTo("updated password");
    }

    @Test
    void handleUserAccountMultifactorEvent() {
        eventPublisher.publishEvent(new ChangeMultiFactorCommand(DEMO_USER, true));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.isTwoFactorEnabled()).isTrue();
    }

    @Test
    void handleUserAccountSettingEvent_theme() {
        eventPublisher.publishEvent(new ChangeUserSettingCommand(
                DEMO_USER,
                ChangeUserSettingCommand.Type.THEME,
                "sample"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getTheme()).isEqualTo("sample");
    }

    @Test
    void handleUserAccountSettingEvent_currency() {
        eventPublisher.publishEvent(new ChangeUserSettingCommand(
                DEMO_USER,
                ChangeUserSettingCommand.Type.CURRENCY,
                "USD"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void handleTokenRegistrationEvent() {
        eventPublisher.publishEvent(new RegisterTokenCommand(
                DEMO_USER.email(),
                "my-refresh-token",
                LocalDateTime.of(2019, 1, 1, 12, 33)));

        var check = entityManager.createQuery("select t from AccountTokenJpa t where t.refreshToken = :token", AccountTokenJpa.class)
                .setParameter("token", "my-refresh-token")
                .getSingleResult();
        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getExpires()).isEqualTo(LocalDateTime.of(2019, 1, 1, 12, 33));
        Assertions.assertThat(check.getRefreshToken()).isEqualTo("my-refresh-token");
        Assertions.assertThat(check.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void handleTokenRevokedEvent() {
        eventPublisher.publishEvent(new RevokeTokenCommand(
                "refresh-token-1"
        ));

        var check = entityManager.find(AccountTokenJpa.class, 1L);
        Assertions.assertThat(check.getExpires().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
