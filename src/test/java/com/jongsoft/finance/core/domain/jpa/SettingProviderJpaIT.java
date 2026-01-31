package com.jongsoft.finance.core.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.core.adapter.api.SettingProvider;
import com.jongsoft.finance.core.domain.commands.SettingUpdatedEvent;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Settings")
class SettingProviderJpaIT extends JpaTestSetup {

    @Inject
    private SettingProvider settingProvider;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql");
    }

    @Test
    @DisplayName("Lookup all settings")
    void lookup() {
        Assertions.assertThat(settingProvider.lookup()).hasSize(6);
    }

    @Test
    @DisplayName("Lookup single setting")
    void lookup_single() {
        var check = settingProvider.lookup("ImportOutdated");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("ImportOutdated");
        Assertions.assertThat(check.get().getValue()).isEqualTo("true");
    }

    @Test
    @DisplayName("Handle setting updated event")
    void handleSettingUpdated() {
        SettingUpdatedEvent.settingUpdated("RegistrationOpen", "false");

        var check = settingProvider.lookup("RegistrationOpen");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getName()).isEqualTo("RegistrationOpen");
        Assertions.assertThat(check.get().getValue()).isEqualTo("false");
    }
}
