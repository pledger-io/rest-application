package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.CSVConfigProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

class CSVConfigProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private CSVConfigProvider csvConfigProvider;

    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();

        StepVerifier.create(csvConfigProvider.lookup())
                .assertNext(batch -> Assertions.assertThat(batch.getFileCode()).isEqualTo("file-code-1"))
                .verifyComplete();
    }

    @Test
    void lookup_name() {
        setup();
        var check = csvConfigProvider.lookup("sample-config");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getFileCode()).isEqualTo("file-code-1");
    }

    @Test
    void lookup_nameIncorrectUser() {
        setup();
        var check = csvConfigProvider.lookup("other-config");

        Assertions.assertThat(check.isPresent()).isFalse();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
