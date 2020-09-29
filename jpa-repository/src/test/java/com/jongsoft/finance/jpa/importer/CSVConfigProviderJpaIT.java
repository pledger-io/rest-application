package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.domain.importer.CSVConfigProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

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
        var check = csvConfigProvider.lookup().test();

        check.assertValueCount(1);
        check.assertValueAt(0, batch -> batch.getFileCode().equals("file-code-1"));
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
