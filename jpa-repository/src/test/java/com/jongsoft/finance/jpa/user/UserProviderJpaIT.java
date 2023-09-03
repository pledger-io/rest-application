package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

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
        Assertions.assertTrue(userProvider.available("user@account"));
        Assertions.assertFalse(userProvider.available("demo-user"));
    }

    @Test
    void lookup() {
        var check = userProvider.lookup("demo-user");

        Assertions.assertTrue(check.isPresent());
        Assertions.assertEquals("demo-user", check.get().getUsername());
        Assertions.assertEquals("1234567", check.get().getPassword());
        Assertions.assertEquals("JBSWY3DPEHPK3PXP", check.get().getSecret());
    }

    @Test
    void lookup_notFound() {
        Assertions.assertFalse(userProvider.lookup("user@account").isPresent());
    }

    @Test
    void tokens() {
        StepVerifier.create(userProvider.tokens("demo-user"))
                .assertNext(token -> "refresh-token-1".equals(token.getToken()))
                .verifyComplete();
    }

    @Test
    void lookup_refreshToken() {
        StepVerifier.create(userProvider.refreshToken("refresh-token-1"))
                .assertNext(a -> "demo-user".equals(a.getUsername()))
                .verifyComplete();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
