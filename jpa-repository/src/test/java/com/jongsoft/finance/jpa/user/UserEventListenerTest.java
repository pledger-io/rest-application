package com.jongsoft.finance.jpa.user;

import java.util.Currency;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jongsoft.finance.domain.user.events.UserAccountCreatedEvent;
import com.jongsoft.finance.domain.user.events.UserAccountMultiFactorEvent;
import com.jongsoft.finance.domain.user.events.UserAccountPasswordChangedEvent;
import com.jongsoft.finance.domain.user.events.UserAccountSettingEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.user.entity.RoleJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import io.micronaut.context.event.ApplicationEventPublisher;

class UserEventListenerTest extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @Test
    void handleUserAccountCreatedEvent() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new UserAccountCreatedEvent(
                this,
                "demo@account",
                "pasword123!"));

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
        eventPublisher.publishEvent(new UserAccountPasswordChangedEvent(
                this,
                "demo-user",
                "updated password"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getPassword()).isEqualTo("updated password");
    }

    @Test
    void handleUserAccountMultifactorEvent() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new UserAccountMultiFactorEvent(
                this,
                "demo-user",
                true));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.isTwoFactorEnabled()).isTrue();
    }

    @Test
    void handleUserAccountSettingEvent_theme() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new UserAccountSettingEvent(
                this,
                "demo-user",
                UserAccountSettingEvent.Type.THEME,
                "sample"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getTheme()).isEqualTo("sample");
    }

    @Test
    void handleUserAccountSettingEvent_currency() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new UserAccountSettingEvent(
                this,
                "demo-user",
                UserAccountSettingEvent.Type.CURRENCY,
                "USD"));

        var check = entityManager.find(UserAccountJpa.class, 1L);
        Assertions.assertThat(check.getCurrency()).isEqualTo(Currency.getInstance("USD"));
    }

}
