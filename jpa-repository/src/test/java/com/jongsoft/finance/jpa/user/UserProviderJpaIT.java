package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class UserProviderJpaIT extends JpaTestSetup {

    @Inject
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql"
        );
    }

    @Test
    void available() {
        Assertions.assertTrue(userProvider.available(new UserIdentifier("user@account")));
        Assertions.assertFalse(userProvider.available(new UserIdentifier("demo-user")));
    }

    @Test
    void lookup() {
        var check = userProvider.lookup(new UserIdentifier("demo-user"));

        Assertions.assertTrue(check.isPresent());
        Assertions.assertEquals("demo-user", check.get().getUsername());
        Assertions.assertEquals("1234567", check.get().getPassword());
        Assertions.assertEquals("JBSWY3DPEHPK3PXP", check.get().getSecret());
    }

    @Test
    void lookup_notFound() {
        Assertions.assertFalse(userProvider.lookup(new UserIdentifier("user@account")).isPresent());
    }

    @Test
    void tokens() {
        assertThat(userProvider.tokens(new UserIdentifier("demo-user")))
                .hasSize(1)
                .first()
                .satisfies(token -> assertThat(token.getToken()).isEqualTo("refresh-token-1"));
    }

    @Test
    void lookup_refreshToken() {
        assertThat(userProvider.refreshToken("refresh-token-1"))
                .hasSize(1)
                .first()
                .satisfies(token -> assertThat(token.getUsername()).isEqualTo(new UserIdentifier("demo-user")));
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
