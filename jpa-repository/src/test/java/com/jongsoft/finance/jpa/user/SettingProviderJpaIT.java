package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

class SettingProviderJpaIT extends JpaTestSetup {

    @Inject
    private SettingProvider settingProvider;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Test
    void lookup() {
        loadDataset("sql/base-setup.sql");
        var check = settingProvider.lookup().test();

        check.assertValueCount(6);
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
