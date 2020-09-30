package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

class CategoryProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private CategoryProviderJpa categoryProviderJpa;

    @Inject
    private FilterFactory filterFactory;

    void setUp() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/user/category-provider.sql"
        );
    }

    @Test
    void lookup() {
        setUp();
        var response = categoryProviderJpa.lookup();

        Assertions.assertThat(response).hasSize(2);
    }

    @Test
    void lookup_byId() {
        setUp();
        var response = categoryProviderJpa.lookup(1L);

        Assertions.assertThat(response.isPresent()).isTrue();
        Assertions.assertThat(response.get().getId()).isEqualTo(1L);
        Assertions.assertThat(response.get().getLabel()).isEqualTo("Grocery");
    }

    @Test
    void lookup_byLabel() {
        setUp();
        var response = categoryProviderJpa.lookup("Grocery").blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void lookup_byFilter() {
        setUp();
        var command = filterFactory.category()
                .label("gro", false);

        var response = categoryProviderJpa.lookup(command);

        Assertions.assertThat(response.total()).isEqualTo(1);
        Assertions.assertThat(response.content().get().getId()).isEqualTo(1L);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}