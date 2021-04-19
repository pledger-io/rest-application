package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.events.*;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.AccountTokenJpa;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.commands.user.ChangeMultiFactorCommand;
import com.jongsoft.finance.messaging.commands.user.ChangePasswordCommand;
import com.jongsoft.finance.messaging.commands.user.ChangeUserSettingCommand;
import com.jongsoft.finance.messaging.commands.user.CreateUserCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

class UserEventListenerIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @Test
    void handleUserAccountCreatedEvent() {
        loadDataset("sql/base-setup.sql");
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
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new ChangePasswordCommand("demo-user", "updated password"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getPassword()).isEqualTo("updated password");
    }

    @Test
    void handleUserAccountMultifactorEvent() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new ChangeMultiFactorCommand("demo-user", true));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.isTwoFactorEnabled()).isTrue();
    }

    @Test
    void handleUserAccountSettingEvent_theme() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new ChangeUserSettingCommand(
                "demo-user",
                ChangeUserSettingCommand.Type.THEME,
                "sample"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getTheme()).isEqualTo("sample");
    }

    @Test
    void handleUserAccountSettingEvent_currency() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new ChangeUserSettingCommand(
                "demo-user",
                ChangeUserSettingCommand.Type.CURRENCY,
                "USD"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void handleTokenRegistrationEvent() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new TokenRegisterEvent(
                "demo-user",
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
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new TokenRevokeEvent(
                "refresh-token-1"
        ));

        var check = entityManager.find(AccountTokenJpa.class, 1L);
        Assertions.assertThat(check.getExpires().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

}
