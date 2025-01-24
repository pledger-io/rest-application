package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SettingProviderJpaIT extends JpaTestSetup {

    @Inject
    private SettingProvider settingProvider;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql");
    }

    @Test
    void lookup() {
        Assertions.assertThat(settingProvider.lookup())
                .hasSize(6);
    }

    @Test
    void lookup_single() {
        var check = settingProvider.lookup("ImportOutdated");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("ImportOutdated");
        Assertions.assertThat(check.get().getValue()).isEqualTo("true");
    }

    @Test
    void handleSettingUpdated() {
        eventPublisher.publishEvent(new SettingUpdatedEvent(
                "RegistrationOpen",
                "false"));

        var check = settingProvider.lookup("RegistrationOpen");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("RegistrationOpen");
        Assertions.assertThat(check.get().getValue()).isEqualTo("false");
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
