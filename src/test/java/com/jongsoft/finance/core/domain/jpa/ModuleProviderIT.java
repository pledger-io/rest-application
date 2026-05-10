package com.jongsoft.finance.core.domain.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.ModuleProvider;
import com.jongsoft.finance.core.domain.model.PledgerModule;

import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Module")
class ModuleProviderIT extends JpaTestSetup {

    @Inject
    private ModuleProvider moduleProvider;

    @Test
    void verifyBankingEnabled() {
        assertThat(moduleProvider.isModuleEnabled("BANKING")).isTrue();
    }

    @Test
    void verifyInvoiceDisabled() {
        assertThat(moduleProvider.isModuleEnabled("INVOICE")).isFalse();
    }

    @Test
    void verifyMissingModule() {
        assertThatThrownBy(() -> moduleProvider.isModuleEnabled("MISSING"))
                .isInstanceOf(StatusException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).contains("Module not found MISSING"));
    }

    @Test
    void verifyBankingModule() {
        PledgerModule module = moduleProvider.getModule("BANKING").get();

        assertThat(module.getId()).isNotNull();
        assertThat(module.getCode()).isEqualTo("BANKING");
        assertThat(module.getRequiredModules()).isEmpty();
        assertThat(module.isEnabled()).isTrue();
    }
}
