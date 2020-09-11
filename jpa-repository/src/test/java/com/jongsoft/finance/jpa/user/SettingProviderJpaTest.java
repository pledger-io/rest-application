package com.jongsoft.finance.jpa.user;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.context.event.ApplicationEventPublisher;

class SettingProviderJpaTest extends JpaTestSetup {

    @Inject
    private SettingProvider settingProvider;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Test
    void lookup() {
        loadDataset("sql/base-setup.sql");
        var check = settingProvider.lookup();

        Assertions.assertThat(check).hasSize(6);
    }

    @Test
    void lookup_single() {
        loadDataset("sql/base-setup.sql");
        var check = settingProvider.lookup("RegistrationOpen");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("RegistrationOpen");
        Assertions.assertThat(check.get().getValue()).isEqualTo("true");
    }

    @Test
    void handleSettingUpdated() {
        loadDataset("sql/base-setup.sql");
        eventPublisher.publishEvent(new SettingUpdatedEvent(
                this,
                "RegistrationOpen",
                "false"));

        var check = settingProvider.lookup("RegistrationOpen");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("RegistrationOpen");
        Assertions.assertThat(check.get().getValue()).isEqualTo("false");
    }
}
