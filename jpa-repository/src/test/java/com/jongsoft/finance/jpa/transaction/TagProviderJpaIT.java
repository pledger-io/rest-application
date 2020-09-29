package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.jpa.FilterFactoryJpa;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

class TagProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TagProvider tagProvider;

    private FilterFactory filterFactory = new FilterFactoryJpa();

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/transaction/tag-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = tagProvider.lookup();

        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    void lookup_name() {
        setup();

        tagProvider.lookup("Nono").test().assertNoValues();
        tagProvider.lookup("Bike").test().assertNoValues();

        Assertions.assertThat(tagProvider.lookup("Sample").blockingGet().name()).isEqualTo("Sample");
    }

    @Test
    void lookup_search() {
        setup();
        var check = tagProvider.lookup(filterFactory.tag().name("mpl", false));
        Assertions.assertThat(check.content()).hasSize(1);

        var fullMatch = tagProvider.lookup(filterFactory.tag().name("Car", true));
        Assertions.assertThat(fullMatch.content()).hasSize(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
