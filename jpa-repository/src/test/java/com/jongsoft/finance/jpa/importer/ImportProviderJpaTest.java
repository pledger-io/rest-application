package com.jongsoft.finance.jpa.importer;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

public class ImportProviderJpaTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ImportProvider importProvider;

    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql",
                "sql/importer/import-provider.sql"
        );
    }

    @Test
    void lookup_slug() {
        setup();
        var check = importProvider.lookup("test-import-1");

        Assertions.assertThat(check.isPresent()).isTrue();
    }

    @Test
    void lookup() {
        setup();
        var check = importProvider.lookup(ImportProvider.FilterCommand.unpaged());

        Assertions.assertThat(check.content()).hasSize(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
