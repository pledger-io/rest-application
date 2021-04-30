package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

class UserProviderJpaIT extends JpaTestSetup {

    @Inject
    private UserProvider userProvider;

    void init() {
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql"
        );
    }

    @Test
    void available() {
        init();
        Assertions.assertTrue(userProvider.available("user@account"));
        Assertions.assertFalse(userProvider.available("demo-user"));
    }

    @Test
    void lookup() {
        init();
        var check = userProvider.lookup("demo-user");

        Assertions.assertTrue(check.isPresent());
        Assertions.assertEquals("demo-user", check.get().getUsername());
        Assertions.assertEquals("1234567", check.get().getPassword());
        Assertions.assertEquals("JBSWY3DPEHPK3PXP", check.get().getSecret());
    }

    @Test
    void lookup_notFound() {
        init();
        Assertions.assertFalse(userProvider.lookup("user@account").isPresent());
    }

    @Test
    void tokens() {
        init();

        userProvider.tokens("demo-user")
                .test()
                .assertComplete()
                .assertValue(token -> "refresh-token-1".equals(token.getToken()));
    }

    @Test
    void lookup_refreshToken() {
        init();

        userProvider.refreshToken("refresh-token-1")
                .test()
                .assertComplete()
                .assertValue(a -> "demo-user".equals(a.getUsername()));
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
