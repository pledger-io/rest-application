package com.jongsoft.finance.core.domain.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.value.UserIdentifier;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Users")
class UserProviderJpaIT extends JpaTestSetup {

    @Inject
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/account/account-provider.sql");
    }

    @Test
    @DisplayName("Check username availability")
    void available() {
        Assertions.assertTrue(userProvider.available(new UserIdentifier("user@account")));
        Assertions.assertFalse(userProvider.available(new UserIdentifier("demo-user")));
    }

    @Test
    @DisplayName("Lookup by the username")
    void lookup() {
        var check = userProvider.lookup(new UserIdentifier("demo-user"));

        Assertions.assertTrue(check.isPresent());
        Assertions.assertEquals("demo-user", check.get().getUsername().email());
        Assertions.assertEquals("1234567", check.get().getPassword());
        Assertions.assertEquals("JBSWY3DPEHPK3PXP", check.get().getSecret());
    }

    @Test
    @DisplayName("Lookup by username - Not Found")
    void lookup_notFound() {
        Assertions.assertFalse(
                userProvider.lookup(new UserIdentifier("user@account")).isPresent());
    }

    @Test
    @DisplayName("Fetch all tokens for user")
    void tokens() {
        assertThat(userProvider.tokens(new UserIdentifier("demo-user")))
                .hasSize(1)
                .first()
                .satisfies(token -> assertThat(token.getToken()).isEqualTo("refresh-token-1"));
    }

    @Test
    @DisplayName("Lookup by refresh token")
    void lookup_refreshToken() {
        assertThat(userProvider.refreshToken("refresh-token-1"))
                .hasSize(1)
                .first()
                .satisfies(token ->
                        assertThat(token.getUsername()).isEqualTo(new UserIdentifier("demo-user")));
    }
}
