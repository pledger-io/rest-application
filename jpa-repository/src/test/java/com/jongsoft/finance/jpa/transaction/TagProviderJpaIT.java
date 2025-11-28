package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TagProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TagProvider tagProvider;

    @Inject
    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/tag-provider.sql"
        );
    }

    @Test
    void lookup() {
        var check = tagProvider.lookup();

        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    void lookup_name() {
        Assertions.assertThat(tagProvider.lookup("Nono")).hasSize(0);
        Assertions.assertThat(tagProvider.lookup("Bike")).hasSize(0);

        Assertions.assertThat(tagProvider.lookup("Sample").get().name()).isEqualTo("Sample");
    }

    @Test
    void lookup_search() {
        var check = tagProvider.lookup(filterFactory.tag().name("mpl", false));
        Assertions.assertThat(check.content()).hasSize(1);

        var fullMatch = tagProvider.lookup(filterFactory.tag().name("Car", true));
        Assertions.assertThat(fullMatch.content()).hasSize(1);
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
