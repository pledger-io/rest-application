package com.jongsoft.finance.jpa.user;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

class UserProviderJpaTest extends JpaTestSetup {

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

}
