package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.ImportConfigurationProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CSVConfigProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ImportConfigurationProvider csvConfigProvider;

    @BeforeEach
    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql"
        );
    }

    @Test
    void lookup() {
        Assertions.assertThat(csvConfigProvider.lookup())
                .hasSize(1)
                .first()
                .satisfies(batch -> {
                    Assertions.assertThat(batch.getFileCode()).isEqualTo("file-code-1");
                    Assertions.assertThat(batch.getType()).isEqualTo("CSVImportProvider");
                });
    }

    @Test
    void lookup_name() {
        var check = csvConfigProvider.lookup("sample-config");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getFileCode()).isEqualTo("file-code-1");
    }

    @Test
    void lookup_nameIncorrectUser() {
        var check = csvConfigProvider.lookup("other-config");

        Assertions.assertThat(check.isPresent()).isFalse();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
